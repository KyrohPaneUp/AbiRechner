package de.kyrohpaneup.abirechner.data

import android.content.Context
import android.util.Log
import de.kyrohpaneup.abirechner.data.grades.Grade
import de.kyrohpaneup.abirechner.data.grades.HeadGrade
import java.util.UUID

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
}

data class CalculationResult(
    val headGrade: HeadGrade,
    val grades: List<Grade>
)