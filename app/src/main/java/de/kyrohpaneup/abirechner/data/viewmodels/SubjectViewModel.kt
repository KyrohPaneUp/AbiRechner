package de.kyrohpaneup.abirechner.data.viewmodels


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import de.kyrohpaneup.abirechner.data.database.HeadGrade
import de.kyrohpaneup.abirechner.data.database.Subject
import de.kyrohpaneup.abirechner.data.database.dao.GradeDao
import de.kyrohpaneup.abirechner.data.database.dao.SubjectDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

class SubjectViewModel(
    private val gradeDao: GradeDao,
    private val subjectDao: SubjectDao
) : ViewModel() {

    private val _subjects = MutableLiveData<List<Subject>>()
    val subjects: LiveData<List<Subject>> = _subjects

    private val _heads = MutableLiveData<List<HeadGrade>>()
    val heads: LiveData<List<HeadGrade>> = _heads

    fun loadData() {
        viewModelScope.launch(Dispatchers.IO) {
            val subjects = subjectDao.getAllSubjects()
            _subjects.postValue(subjects)

            val heads = gradeDao.getAllHeads()
            _heads.postValue(heads)
        }
    }

    fun addSubject(name: String) {
        val subject = UUID.randomUUID().toString()

        viewModelScope.launch(Dispatchers.IO) {
            val newSubject = Subject(subject,
                name,
                examSubject = false,
                doubleWeight = false,
                null)
            subjectDao.insert(newSubject)

            val updated = subjectDao.getAllSubjects()
            _subjects.postValue(updated)
        }
    }
}

class SubjectViewModelFactory(
    private val gradeDao: GradeDao,
    private val subjectDao: SubjectDao
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SubjectViewModel(gradeDao, subjectDao) as T
    }
}
