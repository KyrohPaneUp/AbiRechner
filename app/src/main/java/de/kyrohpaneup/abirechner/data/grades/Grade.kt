package de.kyrohpaneup.abirechner.data.grades

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

@Entity
data class Grade(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "grade") var grade: Int?, // null if calculated
    @ColumnInfo(name = "weight") var weight: Int?,
    @ColumnInfo(name = "name") var name: String?,
    @ColumnInfo(name = "notes") var notes: String?,
    @ColumnInfo(name = "date") var date: String?, // null if calculated
    @ColumnInfo(name = "ignore_grade") var ignoreGrade: Boolean = true, // false if calculated
    @ColumnInfo(name = "parent_grade") var parentGrade: String?,
    @ColumnInfo(name = "head_grade") var headGrade: String?,
    @ColumnInfo(name = "is_calculated") var isCalculated: Boolean = false
)