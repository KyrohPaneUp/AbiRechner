package de.kyrohpaneup.abirechner.data

import de.kyrohpaneup.abirechner.data.database.Grade
import de.kyrohpaneup.abirechner.data.database.HeadGrade
import de.kyrohpaneup.abirechner.data.database.Subject
import java.util.UUID
import kotlin.math.floor
import kotlin.math.round

class GradeManager {

    fun createGrade(parent: String?, head: String?, isCalculated: Boolean): Grade {
        var gradeDouble: Double? = 0.0

        if (isCalculated) {
            gradeDouble = null
        }

        return Grade(
            UUID.randomUUID().toString(),
            gradeDouble,
            0,
            null,
            null,
            null,
            parent,
            head,
            isCalculated
        )
    }

    fun getNumberForPoints(points: Int) = when (points) {
        0 -> "6"
        in 1..15 -> {
            val base = 5 - (points - 1) / 3
            val suffix = arrayOf("-", "", "+")[(points - 1) % 3]
            "$base$suffix"
        }
        else -> "6"
    }

    fun getYearFromId(id: Double?): String {
        if (id == null) return ""
        val year: Int = floor(id).toInt()
        val semester: Int = round(((id - year) * 10)).toInt()
        return "Year $year, semester $semester"
    }

    fun calculateGrades(
        headGrade: HeadGrade,
        gradeList: List<Grade>
    ): CalculationResult {

        val gradeMap: MutableMap<String, Grade> = gradeList.associateBy { it.id }.toMutableMap()

        val childrenMap: Map<String, List<Grade>> = gradeList.groupBy { it.parentGrade as String }

        fun calculate(gradeId: String): Double {
            val children = childrenMap[gradeId] ?: emptyList()

            if (children.isEmpty()) {
                val g = gradeMap[gradeId]
                return g?.grade ?: 0.0
            }

            var weightedSum = 0.0
            var totalWeight = 0

            for (child in children) {
                val childGradeValueNullable = if (child.isCalculated) {
                    calculate(child.id)
                } else {
                    child.grade
                }

                val weight = child.weight ?: 0
                val childGradeValue = childGradeValueNullable ?: 0
                weightedSum += floor(childGradeValue as Double).toInt() * weight
                totalWeight += weight
            }

            val result = if (totalWeight == 0) 0.0 else (weightedSum / totalWeight).toInt()

            gradeMap[gradeId]?.let { g ->
                if (g.isCalculated) {
                    g.grade = result.toDouble()
                }
            }

            return result.toDouble()
        }

        gradeMap.values.filter { it.isCalculated }.forEach {
            calculate(it.id)
        }

        val headValue = calculate(headGrade.id)
        headGrade.grade = floor(headValue).toInt()

        return CalculationResult(
            headGrade = headGrade,
            grades = gradeMap.values.toList()
        )
    }

    fun calculateSubjectGraph(headsById: Map<String, HeadGrade>, gradesByHead: Map<String, List<Grade>>): List<GradeGraphResult> {
        val results = mutableListOf<GradeGraphResult>()
        for (head in gradesByHead.keys) {
            val result =
                headsById[head]?.year?.let {
                    GradeGraphResult(
                        it, calculateParent(head, gradesByHead.getOrDefault(head,
                            emptyList())))
                }
            if (result != null) {
                results.add(result)
            }
        }
        return results
    }

    private fun calculateParent(head: String, grades: List<Grade>): Double {
        var totalGrade = 0.0
        var totalWeight = 0.0

        for (grade in grades) {
            if (grade.isCalculated) {
                val children = grades.filter { it.parentGrade == grade.id }
                grade.grade = calculateParent(grade.id, children)
            }
            if (grade.parentGrade == head) {
                totalGrade += grade.grade?.times((grade.weight?.toDouble()!! / 100)) ?: totalGrade
                totalWeight += grade.weight?.toDouble()!!
            }
        }
        if (totalWeight == 0.0) return 0.0
        return totalGrade / (totalWeight / 100)
    }

    fun calculateGradeGraph(
        head: String,
        grades: List<Grade>
    ): List<GradeGraphResult> {
        val gradesByDate = grades
            .filter {  it.date != null }
            .groupBy { it.date!! }
            .toSortedMap()

        if (gradesByDate.isEmpty()) return emptyList()

        val resultList = mutableListOf<GradeGraphResult>()

        fun calculate(grade: Grade): Double {
            val children = grades
                .filter { it.date != null && grade.date != null && it.date!! <= grade.date!! }

            val usedGradesByParent = children
                .filter { it.parentGrade != null }
                .filter { it.headGrade != null }
                .filter { it.parentGrade != it.headGrade }
                .groupBy { it.parentGrade }
                .toMap()

            val parents: MutableList<Grade> = mutableListOf()
            val usedGrades: MutableList<Grade> = mutableListOf()

            for (parentId in usedGradesByParent.keys) {
                val parent = grades.first { it.id == parentId }
                parents.add(parent)
            }

            usedGrades.addAll(children)
            usedGrades.addAll(parents)

            return calculateParent(head, usedGrades)
        }

        for (grade in gradesByDate) {
            resultList.add(GradeGraphResult(grade.key, calculate(grade.value.first())))
        }

        return resultList
    }

    fun calculateE1Grade(subjects: List<Subject>, heads: List<HeadGrade>): Int {
        val pointSum = 40 * calculateALevelsTotalPoints(subjects, heads)
        val finalHeads = heads.filter { it.year != null && it.year!! >= 12.0 }
        val division = finalHeads.size + finalHeads.sumOf { head ->
            subjects.count { it.doubleWeight && it.id == head.subject }
        }
        return if (division == 0) 0 else pointSum / division
    }

    fun calculateE2Grade(subjects: List<Subject>): Int {
        var pointSum = 0
        subjects.filter { it.examSubject && it.examGrade != null }.forEach {
            pointSum += it.examGrade?.times(4) ?: 0
        }
        return pointSum
    }

    private fun calculateALevelsTotalPoints(subjects: List<Subject>, heads: List<HeadGrade>): Int {
        val headsBySubject = heads.groupBy { it.subject }
        val subjectById = subjects.associateBy { it.id }

        var points = 0

        for (subject in subjectById.values) {
            val isDoubleWeight = subject.doubleWeight
            val headGrades = headsBySubject.getOrDefault(subject.id, emptyList()).filter { it.year != null && it.year!! >= 12.0}
            for (headGrade in headGrades) {
                if (headGrade.grade == null) continue
                points += if (isDoubleWeight) (headGrade.grade!! * 2)
                else headGrade.grade!!
            }
        }

        return points
    }

    fun calculateAverageFromPoints(points: Int): Double? {
        return when {
            points >= 823 -> 1.0
            points >= 805 -> 1.1
            points >= 787 -> 1.2
            points >= 769 -> 1.3
            points >= 751 -> 1.4
            points >= 733 -> 1.5
            points >= 715 -> 1.6
            points >= 697 -> 1.7
            points >= 679 -> 1.8
            points >= 661 -> 1.9
            points >= 643 -> 2.0
            points >= 625 -> 2.1
            points >= 607 -> 2.2
            points >= 589 -> 2.3
            points >= 571 -> 2.4
            points >= 553 -> 2.5
            points >= 535 -> 2.6
            points >= 517 -> 2.7
            points >= 499 -> 2.8
            points >= 481 -> 2.9
            points >= 463 -> 3.0
            points >= 445 -> 3.1
            points >= 427 -> 3.2
            points >= 409 -> 3.3
            points >= 391 -> 3.4
            points >= 373 -> 3.5
            points >= 355 -> 3.6
            points >= 337 -> 3.7
            points >= 319 -> 3.8
            points >= 301 -> 3.9
            points == 300 -> 4.0

            else -> { null }
        }
    }
}

data class CalculationResult(
    val headGrade: HeadGrade,
    val grades: List<Grade>
)

data class GradeGraphResult(
    val x: Number,
    val y: Double
)