package de.kyrohpaneup.abirechner.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity
data class HeadGrade(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "subject") val subject: String?,
    @ColumnInfo(name = "teacher") var teacher: String?,
    @ColumnInfo(name = "year") var year: Double?,
    @ColumnInfo(name = "grade") var grade: Int?)