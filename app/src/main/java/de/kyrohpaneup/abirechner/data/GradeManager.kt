package de.kyrohpaneup.abirechner.data

import android.util.Log
import de.kyrohpaneup.abirechner.data.database.Grade
import de.kyrohpaneup.abirechner.data.database.HeadGrade
import java.util.UUID
import kotlin.math.floor
import kotlin.math.round

class GradeManager {

    fun createGrade(parent: String?, head: String?, isCalculated: Boolean): Grade {
        var gradeInt: Int? = 0

        if (isCalculated) {
            gradeInt = null
        }

        return Grade(
            UUID.randomUUID().toString(),
            gradeInt,
            0,
            null,
            null,
            null,
            !isCalculated,
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

        fun calculate(gradeId: String): Int {
            val children = childrenMap[gradeId] ?: emptyList()

            if (children.isEmpty()) {
                val g = gradeMap[gradeId]
                return g?.grade ?: 0
            }

            var weightedSum = 0.0
            var totalWeight = 0

            for (child in children) {
                val _childGradeValue = if (child.isCalculated) {
                    calculate(child.id)
                } else {
                    child.grade
                }

                var weight = child.weight ?: 0
                if (child.ignoreGrade) weight = 0
                val childGradeValue = _childGradeValue ?: 0
                weightedSum += childGradeValue * weight
                totalWeight += weight
            }

            val result = if (totalWeight == 0) 0 else (weightedSum / totalWeight).toInt()

            gradeMap[gradeId]?.let { g ->
                if (g.isCalculated) {
                    g.grade = result
                }
            }

            return result
        }

        gradeMap.values.filter { it.isCalculated }.forEach {
            calculate(it.id)
        }

        val headValue = calculate(headGrade.id)
        headGrade.grade = headValue

        return CalculationResult(
            headGrade = headGrade,
            grades = gradeMap.values.toList()
        )
    }

    fun getGradeGraph(
        headGrade: HeadGrade,
        grades: List<Grade>
    ): List<GradeGraphResult> {
        val gradesByDate = grades
            .filter { it.date != null }
            .groupBy { it.date!! }
            .toSortedMap()

        Log.i("GM", "list = empty? ${gradesByDate.isEmpty()}")

        if (gradesByDate.isEmpty()) return emptyList()

        val resultList = mutableListOf<GradeGraphResult>()
        val accumulatedGrades = mutableListOf<Grade>()

        for ((date, newGrades) in gradesByDate) {
            accumulatedGrades.addAll(newGrades)

            val gradeMap = accumulatedGrades.associateBy { it.id }.toMutableMap()
            val childrenMap = accumulatedGrades.groupBy { it.parentGrade as String }

            fun calculate(gradeId: String): Double {
                val children = childrenMap[gradeId] ?: emptyList()

                if (children.isEmpty()) {
                    return (gradeMap[gradeId]?.grade ?: 0).toDouble()
                }

                var weightedSum = 0.0
                var totalWeight = 0

                for (child in children) {
                    val childValue = if (child.isCalculated) {
                        calculate(child.id)
                    } else {
                        (child.grade ?: 0).toDouble()
                    }

                    val weight = if (child.ignoreGrade) 0 else (child.weight ?: 0)

                    weightedSum += childValue * weight
                    totalWeight += weight
                }

                return if (totalWeight == 0) 0.0 else weightedSum / totalWeight
            }

            val headValue = calculate(headGrade.id)
            resultList.add(GradeGraphResult(date, headValue))
        }

        Log.i("GM", "Finished calculating")
        return resultList
    }
}

data class CalculationResult(
    val headGrade: HeadGrade,
    val grades: List<Grade>
)

data class GradeGraphResult(
    val x: Long,
    val y: Double
)