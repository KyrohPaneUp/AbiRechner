package de.kyrohpaneup.abirechner.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    indices = [
        Index(value = ["name"], unique = true)
    ]
)
data class Subject(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "name") var name: String?,
    @ColumnInfo(name = "exam_subject") var examSubject: Boolean = false,
    @ColumnInfo(name = "double_weight") var doubleWeight: Boolean = false,
    @ColumnInfo(name = "exam_grade") var examGrade: Int?
)