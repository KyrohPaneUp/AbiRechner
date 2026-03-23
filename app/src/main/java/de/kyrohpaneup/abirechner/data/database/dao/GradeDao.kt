package de.kyrohpaneup.abirechner.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import de.kyrohpaneup.abirechner.data.database.Grade
import de.kyrohpaneup.abirechner.data.database.HeadGrade

@Dao
interface GradeDao {
    // Grades
    @Insert
    suspend fun insert(vararg grade: Grade)

    @Delete
    suspend fun delete(vararg grade: Grade)

    @Update
    suspend fun update(vararg grade: Grade)

    @Query("SELECT * FROM grade")
    suspend fun getAll(): List<Grade>

    @Query("SELECT * FROM grade WHERE id = :gid")
    suspend fun getGradeFromId(gid: String): List<Grade>

    @Query("SELECT * FROM grade WHERE parent_grade = :pgrade")
    suspend fun getChildGrades(pgrade: String): List<Grade>

    @Query("SELECT * FROM grade WHERE head_grade = :hgrade")
    suspend fun getAllGradesForHead(hgrade: String): List<Grade>

    @Query("DELETE FROM grade WHERE head_grade = :hgrade")
    suspend fun deleteAllGradesForHead(hgrade: String)

    // Head Grades
    @Query("SELECT * FROM headgrade")
    suspend fun getAllHeads(): List<HeadGrade>

    @Insert
    suspend fun insertHead(vararg headGrade: HeadGrade)

    @Delete
    suspend fun deleteHead(vararg headGrade: HeadGrade)

    @Update
    suspend fun updateHead(vararg headGrade: HeadGrade)

    @Query("SELECT * FROM headgrade WHERE id = :hId")
    suspend fun getHeadFromId(hId: String): List<HeadGrade>
}