package de.kyrohpaneup.abirechner.data.viewmodels

import androidx.lifecycle.*
import de.kyrohpaneup.abirechner.data.GradeManager
import de.kyrohpaneup.abirechner.data.database.Grade
import de.kyrohpaneup.abirechner.data.database.dao.GradeDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChildGradeViewModel(
    private val dao: GradeDao
) : ViewModel() {

    private val _grade = MutableLiveData<Grade>()
    val grade: LiveData<Grade> = _grade

    fun loadGrade(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val g = dao.getGradeFromId(id).firstOrNull()
            g?.let { _grade.postValue(it) }
        }
    }

    fun updateGrade(updated: Grade) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.update(updated)
            val gradeManager = GradeManager()
            if (updated.headGrade == null) return@launch
            val headGrade = dao.getHeadFromId(updated.headGrade!!).firstOrNull()
            if (headGrade != null) {
                val gradeList = dao.getAllGradesForHead(headGrade.id)
                val calculationResult = gradeManager.calculateGrades(headGrade, gradeList)
                for (grade in calculationResult.grades) {
                    dao.update(grade)
                }
                dao.updateHead(calculationResult.headGrade)
            }
        }
    }

}

class ChildGradeViewModelFactory(
    private val dao: GradeDao
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ChildGradeViewModel(dao) as T
    }
}