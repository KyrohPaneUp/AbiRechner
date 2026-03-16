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

    private val _subject = MutableLiveData<Subject>()
    val subject: LiveData<Subject> = _subject

    fun loadData(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val grades = subjectDao.getHeadsForSubject(id)
            _grades.postValue(grades)

            val subject = subjectDao.getSubjectFromId(id).firstOrNull()
            if (subject != null) _subject.postValue(subject!!)
        }
    }

    fun addHeadGrade() {
        val head = UUID.randomUUID().toString()

        viewModelScope.launch(Dispatchers.IO) {
            val newGrade = HeadGrade(head, subject.value?.id, null, null ,0)
            gradeDao.insertHead(newGrade)

            val subjectId = subject.value?.id ?: ""
            val updated = subjectDao.getHeadsForSubject(subjectId)
            _grades.postValue(updated)
        }
    }

    fun getSubjectName(): String {
        if (subject.value?.name == null) {
            return "N/A"
        }
        return subject.value?.name!!
    }
}

class GradeViewModelFactory(
    private val gradeDao: GradeDao,
    private val subjectDao: SubjectDao
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return GradeViewModel(gradeDao, subjectDao) as T
    }
}
