package de.kyrohpaneup.abirechner.data.viewmodels

import androidx.lifecycle.*
import de.kyrohpaneup.abirechner.data.GradeManager
import de.kyrohpaneup.abirechner.data.database.Grade
import de.kyrohpaneup.abirechner.data.database.dao.GradeDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ParentGradeViewModel(
    private val dao: GradeDao
) : ViewModel() {

    private val _parentGrade = MutableLiveData<Grade>()
    val parentGrade: LiveData<Grade> = _parentGrade

    private val _childGrades = MutableLiveData<List<Grade>>()
    val childGrades: LiveData<List<Grade>> = _childGrades

    private var gradeId: String = ""
    private var headGradeId: String? = null

    fun loadParent(gradeId: String) {
        this.gradeId = gradeId

        viewModelScope.launch(Dispatchers.IO) {
            val parent = dao.getGradeFromId(gradeId).firstOrNull()
            parent?.let {
                headGradeId = it.headGrade
                _parentGrade.postValue(it)
            }

            val children = dao.getChildGrades(gradeId)
            _childGrades.postValue(children)
        }
    }

    fun addChildGrade(isCalculated: Boolean) {
        val parentId = gradeId
        val headId = headGradeId

        viewModelScope.launch(Dispatchers.IO) {
            val manager = GradeManager()
            val newGrade = manager.createGrade(parentId, headId, isCalculated)
            dao.insert(newGrade)

            val updated = dao.getChildGrades(parentId)
            _childGrades.postValue(updated ?: emptyList())
        }
    }

    fun updateParent(name: String, notes: String, weight: Int) {
        val current = _parentGrade.value ?: return

        current.name = name
        current.notes = notes
        current.weight = weight

        viewModelScope.launch(Dispatchers.IO) {
            dao.update(current)

            val gradeManager = GradeManager()
            if (current.headGrade == null) return@launch
            val headGrade = dao.getHeadFromId(current.headGrade!!).firstOrNull()
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

    fun deleteParent() {
        val current = _parentGrade.value ?: return

        viewModelScope.launch(Dispatchers.IO) {
            dao.delete(current)

            dao.getChildGrades(current.id)?.forEach {
                dao.delete(it)
            }
        }
    }

    fun getHeadGradeId(): String? = headGradeId
}

class ParentGradeViewModelFactory(
    private val dao: GradeDao
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ParentGradeViewModel(dao) as T
    }
}