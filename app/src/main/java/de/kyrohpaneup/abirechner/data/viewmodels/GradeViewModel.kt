package de.kyrohpaneup.abirechner.data.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import de.kyrohpaneup.abirechner.data.database.dao.GradeDao
import de.kyrohpaneup.abirechner.data.database.HeadGrade
import de.kyrohpaneup.abirechner.data.database.Subject
import de.kyrohpaneup.abirechner.data.database.dao.SubjectDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

class GradeViewModel(
    private val gradeDao: GradeDao,
    private val subjectDao: SubjectDao
) : ViewModel() {

    private val _grades = MutableLiveData<List<HeadGrade>>()
    val grades: LiveData<List<HeadGrade>> = _grades

    private val _subjects = MutableLiveData<List<Subject>>()
    val subjects: LiveData<List<Subject>> = _subjects

    fun loadData() {
        viewModelScope.launch(Dispatchers.IO) {
            val grades = gradeDao.getAllHeads()
            _grades.postValue(grades)

            val subjects = subjectDao.getAllSubjects()
            _subjects.postValue(subjects)
        }
    }

    fun addHeadGrade() {
        val head = UUID.randomUUID().toString()

        viewModelScope.launch(Dispatchers.IO) {
            val newGrade = HeadGrade(head, null, null, null ,0)
            gradeDao.insertHead(newGrade)

            val updated = gradeDao.getAllHeads()
            _grades.postValue(updated)
        }
    }

    fun addSubject(name: String) {
        val subject = UUID.randomUUID().toString()

        viewModelScope.launch(Dispatchers.IO) {
            val newSubject = Subject(subject, name)
            subjectDao.insert(newSubject)

            val updated = subjectDao.getAllSubjects()
            _subjects.postValue(updated)
            Log.d("Subjects","inserted subject $name")
        }
    }

    fun getSubjectFromId(id: String): String =
        subjects.value?.find { it.id == id }?.name ?: "N/A"
}

class GradeViewModelFactory(
    private val gradeDao: GradeDao,
    private val subjectDao: SubjectDao
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return GradeViewModel(gradeDao, subjectDao) as T
    }
}
