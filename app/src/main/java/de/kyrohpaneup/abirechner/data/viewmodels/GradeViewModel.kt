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
import java.util.UUID

class GradeViewModel(
    private val dao: GradeDao
) : ViewModel() {

    private val _grades = MutableLiveData<List<HeadGrade>>()
    val grades: LiveData<List<HeadGrade>> = _grades

    fun loadGrades() {
        viewModelScope.launch(Dispatchers.IO) {
            val grades = dao.getAllHeads()
            _grades.postValue(grades)
        }
    }

    fun addHeadGrade() {
        val head = UUID.randomUUID().toString()

        viewModelScope.launch(Dispatchers.IO) {
            val newGrade = HeadGrade(head, null, null, null ,0)
            dao.insertHead(newGrade)

            val updated = dao.getAllHeads()
            _grades.postValue(updated ?: emptyList())
        }
    }
}

class GradeViewModelFactory(
    private val dao: GradeDao
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return GradeViewModel(dao) as T
    }
}
