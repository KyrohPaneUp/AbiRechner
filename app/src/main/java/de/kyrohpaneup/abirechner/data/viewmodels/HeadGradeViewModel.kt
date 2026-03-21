package de.kyrohpaneup.abirechner.data.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import de.kyrohpaneup.abirechner.data.GradeManager
import de.kyrohpaneup.abirechner.data.database.Grade
import de.kyrohpaneup.abirechner.data.database.dao.GradeDao
import de.kyrohpaneup.abirechner.data.database.HeadGrade
import de.kyrohpaneup.abirechner.data.database.Subject
import de.kyrohpaneup.abirechner.data.database.dao.SubjectDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HeadGradeViewModel(
    private val dao: GradeDao,
    private val subjectDao: SubjectDao
) : ViewModel() {

    private val _headGrade = MutableLiveData<HeadGrade>()
    val headGrade: LiveData<HeadGrade> = _headGrade

    private val _subject = MutableLiveData<Subject>()
    val subject: LiveData<Subject> = _subject

    private val _childGrades = MutableLiveData<List<Grade>>()
    val childGrades: LiveData<List<Grade>> = _childGrades

    private val _allGrades = MutableLiveData<List<Grade>>()
    val allGrades: LiveData<List<Grade>> = _allGrades

    private var headGradeId: String = ""

    fun loadHead(id: String) {
        this.headGradeId = id

        viewModelScope.launch(Dispatchers.IO) {
            val head = dao.getHeadFromId(id).firstOrNull()
            head?.let { _headGrade.postValue(it) }

            val children = dao.getChildGrades(id)
            _childGrades.postValue(children)

            val allGrades = dao.getAllGradesForHead(id)
            _allGrades.postValue(allGrades)

            if (head?.subject != null) {
                val subject = subjectDao.getSubjectFromId(head.subject).firstOrNull()
                subject?.let { _subject.postValue(it) }
            }
        }
    }

    fun addChildGrade(isCalculated: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val manager = GradeManager()
            val newGrade = manager.createGrade(headGradeId, headGradeId, isCalculated)
            dao.insert(newGrade)

            val updated = dao.getChildGrades(headGradeId)
            _childGrades.postValue(updated)
        }
    }

    fun updateHead(teacher: String, year: Double?) {
        val current = _headGrade.value ?: return
        current.teacher = teacher
        current.year = year

        viewModelScope.launch(Dispatchers.IO) {
            dao.updateHead(current)

            val gradeManager = GradeManager()

            val gradeList = dao.getAllGradesForHead(current.id)
            val calculationResult = gradeManager.calculateGrades(current, gradeList)
            for (grade in calculationResult.grades) {
                dao.update(grade)
            }
            dao.updateHead(calculationResult.headGrade)
        }
    }

    fun deleteHead() {
        val current = _headGrade.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            dao.deleteHead(current)

            dao.getChildGrades(current.id).forEach {
                dao.delete(it)
            }
        }
    }

    fun getHeadGradeId(): String = headGradeId
}

class HeadGradeViewModelFactory(
    private val dao: GradeDao,
    private val subjectDao: SubjectDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HeadGradeViewModel(dao, subjectDao) as T
    }
}