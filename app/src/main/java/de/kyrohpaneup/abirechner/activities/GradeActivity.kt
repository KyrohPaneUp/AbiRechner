package de.kyrohpaneup.abirechner.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModelProvider
import de.kyrohpaneup.abirechner.R
import de.kyrohpaneup.abirechner.adapters.HeadGradeAdapter
import de.kyrohpaneup.abirechner.data.database.AppDatabase
import de.kyrohpaneup.abirechner.data.database.dao.GradeDao
import de.kyrohpaneup.abirechner.data.database.HeadGrade
import de.kyrohpaneup.abirechner.data.viewmodels.GradeViewModel
import de.kyrohpaneup.abirechner.data.viewmodels.GradeViewModelFactory

class GradeActivity : ComponentActivity() {
    private var grades: MutableList<HeadGrade> = mutableListOf()
    private lateinit var listView: ListView
    private var context: Context = this
    private lateinit var button: Button
    private lateinit var arrayAdapter: HeadGradeAdapter

    private lateinit var viewModel: GradeViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.main_activity_layout)

        bindViews()
        setupList()

        val dao: GradeDao = AppDatabase.getDatabase(this).gradeDao()
        val factory = GradeViewModelFactory(dao)
        viewModel = ViewModelProvider(this, factory)[GradeViewModel::class]

        viewModel.loadGrades()

        observeViewModel()
        setupListeners()
    }

    private fun bindViews() {
        listView = findViewById(R.id.listView)
        button = findViewById(R.id.button)
    }

    private fun setupList() {
        arrayAdapter = HeadGradeAdapter(context, grades)
        listView.adapter = arrayAdapter

        listView.setOnItemClickListener { _, _, position, _ ->
            val clickedHeadGrade = grades[position]
            openHeadGradeActivity(clickedHeadGrade)
        }
    }

    private fun observeViewModel() {
        viewModel.grades.observe(this) { list ->
            grades.clear()
            grades.addAll(list)
            arrayAdapter.notifyDataSetChanged()
        }
    }

    private fun setupListeners() {
        button.setOnClickListener {
            viewModel.addHeadGrade()
        }
    }

    private fun openHeadGradeActivity(headGrade: HeadGrade) {
        val intent = Intent(this, HeadGradeActivity::class.java)
        intent.putExtra("headGradeId", headGrade.id)
        startActivity(intent)
    }
}
