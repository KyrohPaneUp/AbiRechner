package de.kyrohpaneup.abirechner.data.grades

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity
data class HeadGrade(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "subject") var subject: String?,
    @ColumnInfo(name = "teacher") var teacher: String?,
    @ColumnInfo(name = "year") var year: String?,
    @ColumnInfo(name = "grade") var grade: Int?)