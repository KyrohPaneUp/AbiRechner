package de.kyrohpaneup.abirechner.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.compose.material3.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import de.kyrohpaneup.abirechner.R
import de.kyrohpaneup.abirechner.data.AppDatabase
import de.kyrohpaneup.abirechner.data.GradeManager
import de.kyrohpaneup.abirechner.data.grades.Grade
import de.kyrohpaneup.abirechner.data.grades.GradeDao
import de.kyrohpaneup.abirechner.data.grades.HeadGrade
import de.kyrohpaneup.abirechner.data.viewmodels.HeadGradeViewModel
import de.kyrohpaneup.abirechner.data.viewmodels.HeadGradeViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class HeadGradeActivity : ComponentActivity() {

    private var context: Context = this

    private lateinit var listView: ListView
    private lateinit var subjectView: EditText
    private lateinit var teacherView: EditText
    private lateinit var yearView: EditText


    private lateinit var addGradeButton: Button
    private lateinit var saveButton: Button
    private lateinit var deleteButton: Button

    private lateinit var gradeNumberView: TextView
    private lateinit var gradePointsView: TextView

    private lateinit var viewModel: HeadGradeViewModel

    private val grades: MutableList<Grade> = mutableListOf()
    private lateinit var arrayAdapter: GradeAdapter

    private var headGradeId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.headgrade_activity_layout)

        onBackPressedDispatcher.addCallback(this) {
            goToParent()
        }

        bindViews()
        setupList()

        headGradeId = intent.getStringExtra("headGradeId") ?: ""

        val dao = AppDatabase.getDatabase(this).gradeDao()
        val factory = HeadGradeViewModelFactory(dao)
        viewModel = ViewModelProvider(this, factory)[HeadGradeViewModel::class]

        observeViewModel()
        setupListeners()

        viewModel.loadHead(headGradeId)
    }

    fun bindViews() {
        this.subjectView = findViewById(R.id.subjectText)
        this.teacherView = findViewById(R.id.teacher_text)
        this.yearView = findViewById(R.id.year_text)

        this.addGradeButton = findViewById(R.id.addGradeButton)
        this.saveButton = findViewById(R.id.save_button)
        this.deleteButton = findViewById(R.id.delete_button)

        this.listView = findViewById(R.id.listView)

        this.gradeNumberView = findViewById(R.id.grade_number_view)
        this.gradePointsView = findViewById(R.id.grade_points_view)
    }

    private fun setupList() {
        arrayAdapter = GradeAdapter(this, grades)
        listView.adapter = arrayAdapter

        listView.setOnItemClickListener { _, _, position, _ ->
            val clicked = grades[position]
            openGradeActivity(clicked)
        }
    }

    private fun observeViewModel() {
        viewModel.headGrade.observe(this) { grade ->
            headGradeId = viewModel.getHeadGradeId()

            subjectView.setText(grade.subject)
            teacherView.setText(grade.teacher)
            yearView.setText(grade.year)

            val points = grade.grade ?: 0
            updateGradeUI(points)
        }

        viewModel.childGrades.observe(this) { list ->
            grades.clear()
            grades.addAll(list)
            arrayAdapter.notifyDataSetChanged()
        }
    }

    private fun setupListeners() {

        addGradeButton.setOnClickListener {
            val options = arrayOf("Calculated Grade", "Static Grade")

            MaterialAlertDialogBuilder(this)
                .setTitle("Create Grade")
                .setItems(options) { _, which ->
                    val isCalculated = which == 0
                    viewModel.addChildGrade(isCalculated)
                }
                .show()
        }

        saveButton.setOnClickListener {
            viewModel.updateHead(
                subjectView.text.toString(),
                teacherView.text.toString(),
                yearView.text.toString()
            )

            goToParent()
        }

        deleteButton.setOnClickListener {
            viewModel.deleteHead()
            goToParent()
        }
    }

    private fun updateGradeUI(points: Int) {
        val manager = GradeManager()
        gradeNumberView.text = manager.getNumberForPoints(points)
        gradePointsView.text = points.toString()
    }

    private fun openGradeActivity(grade: Grade) {
        lateinit var intent: Intent
        if (grade.isCalculated) {
            intent = Intent(this, ParentGradeActivity::class.java)
        } else {
            intent = Intent(this, ChildGradeActivity::class.java)
        }
        intent.putExtra("gradeId", grade.id)
        startActivity(intent)
    }

    fun goToParent() {
        finish()
        val intent = Intent(this, GradeActivity::class.java)
        startActivity(intent)
    }
}