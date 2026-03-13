package de.kyrohpaneup.abirechner.activities

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModelProvider
import de.kyrohpaneup.abirechner.R
import de.kyrohpaneup.abirechner.adapters.HeadGradeAdapter
import de.kyrohpaneup.abirechner.adapters.SubjectAdapter
import de.kyrohpaneup.abirechner.data.database.AppDatabase
import de.kyrohpaneup.abirechner.data.database.dao.GradeDao
import de.kyrohpaneup.abirechner.data.database.HeadGrade
import de.kyrohpaneup.abirechner.data.database.Subject
import de.kyrohpaneup.abirechner.data.database.dao.SubjectDao
import de.kyrohpaneup.abirechner.data.viewmodels.GradeViewModel
import de.kyrohpaneup.abirechner.data.viewmodels.GradeViewModelFactory

class GradeActivity : ComponentActivity() {
    private var grades: MutableList<HeadGrade> = mutableListOf()
    private var subjects: MutableList<Subject> = mutableListOf()
    private var context: Context = this
    private lateinit var gradeButton: Button
    private lateinit var subjectButton: Button
    private lateinit var gradeListView: ListView
    private lateinit var subjectListView: ListView
    private lateinit var gradeAdapter: HeadGradeAdapter
    private lateinit var subjectAdapter: SubjectAdapter


    private lateinit var viewModel: GradeViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.main_activity_layout)

        bindViews()
        setupList()

        val gradeDao: GradeDao = AppDatabase.getDatabase(this).gradeDao()
        val subjectDao: SubjectDao = AppDatabase.getDatabase(this).subjectDao()
        val factory = GradeViewModelFactory(gradeDao, subjectDao)
        viewModel = ViewModelProvider(this, factory)[GradeViewModel::class]

        viewModel.loadData()

        observeViewModel()
        setupListeners()
    }

    private fun bindViews() {
        gradeListView = findViewById(R.id.grade_list_view)
        gradeButton = findViewById(R.id.grade_button)
        subjectListView = findViewById(R.id.subject_list_view)
        subjectButton = findViewById(R.id.subject_button)
    }

    private fun setupList() {
        gradeAdapter = HeadGradeAdapter(context, grades)  { id -> viewModel.getSubjectFromId(id)}
        gradeListView.adapter = gradeAdapter

        gradeListView.setOnItemClickListener { _, _, position, _ ->
            val clickedHeadGrade = grades[position]
            openHeadGradeActivity(clickedHeadGrade)
        }

        subjectAdapter = SubjectAdapter(context, subjects)
        subjectListView.adapter = subjectAdapter

        subjectListView.setOnItemClickListener { _, _, position, _ ->
            //val clickedHeadGrade = grades[position]
            //openHeadGradeActivity(clickedHeadGrade)
        }
    }

    private fun observeViewModel() {
        viewModel.grades.observe(this) { list ->
            grades.clear()
            grades.addAll(list)
            gradeAdapter.notifyDataSetChanged()
        }

        viewModel.subjects.observe(this) { list ->
            subjects.clear()
            subjects.addAll(list)
            subjectAdapter.notifyDataSetChanged()
        }
    }

    private fun setupListeners() {
        gradeButton.setOnClickListener {
            viewModel.addHeadGrade()
        }
        subjectButton.setOnClickListener() {
            showTextInputDialog()
        }
    }

    private fun openHeadGradeActivity(headGrade: HeadGrade) {
        val intent = Intent(this, HeadGradeActivity::class.java)
        intent.putExtra("headGradeId", headGrade.id)
        startActivity(intent)
    }

    private fun showTextInputDialog() {
        val input = EditText(this)
        input.hint = "Enter text here"

        AlertDialog.Builder(this)
            .setTitle("Enter Subject")
            .setMessage("Please enter the subject name:")
            .setView(input)
            .setPositiveButton("Enter") { dialog, _ ->
                val text = input.text.toString()
                if (text.isNotEmpty()) {
                    viewModel.addSubject(text)
                } else {
                    Toast.makeText(this, "Please enter some name", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}
