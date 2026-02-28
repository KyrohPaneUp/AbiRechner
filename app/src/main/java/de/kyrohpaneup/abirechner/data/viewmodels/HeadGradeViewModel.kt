package de.kyrohpaneup.abirechner.data.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import de.kyrohpaneup.abirechner.data.GradeManager
import de.kyrohpaneup.abirechner.data.grades.Grade
import de.kyrohpaneup.abirechner.data.grades.GradeDao
import de.kyrohpaneup.abirechner.data.grades.HeadGrade
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HeadGradeViewModel(
    private val dao: GradeDao
) : ViewModel() {

    private val _headGrade = MutableLiveData<HeadGrade>()
    val headGrade: LiveData<HeadGrade> = _headGrade

    private val _childGrades = MutableLiveData<List<Grade>>()
    val childGrades: LiveData<List<Grade>> = _childGrades

    private var headGradeId: String = ""

    fun loadHead(id: String) {
        this.headGradeId = id

        viewModelScope.launch(Dispatchers.IO) {
            val head = dao.getHeadFromId(id).firstOrNull()
            head?.let { _headGrade.postValue(it) }

            val children = dao.getChildGrades(id)
            _childGrades.postValue(children)
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

    fun updateHead(subject: String, teacher: String, year: String) {
        val current = _headGrade.value ?: return
        current.subject = subject
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

            dao.getChildGrades(current.id)?.forEach {
                dao.delete(it)
            }
        }
    }

    fun getHeadGradeId(): String = headGradeId
}

class HeadGradeViewModelFactory(
    private val dao: GradeDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HeadGradeViewModel(dao) as T
    }
}