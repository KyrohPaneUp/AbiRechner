package de.kyrohpaneup.abirechner.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import de.kyrohpaneup.abirechner.data.database.HeadGrade
import de.kyrohpaneup.abirechner.data.database.Subject

@Dao
interface SubjectDao {

    @Insert
    suspend fun insert(subject: Subject)

    @Delete
    suspend fun delete(subject: Subject)

    @Update
    suspend fun update(subject: Subject)

    @Query("SELECT * FROM Subject WHERE id = :id")
    suspend fun getSubjectFromId(id: String): List<Subject>

    @Query("SELECT * FROM Subject")
    suspend fun getAllSubjects(): List<Subject>

    @Query("SELECT * FROM HeadGrade WHERE subject = :id")
    suspend fun getHeadsForSubject(id: String): List<HeadGrade>
}