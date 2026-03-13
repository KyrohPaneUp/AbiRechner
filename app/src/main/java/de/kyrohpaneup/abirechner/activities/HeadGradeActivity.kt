package de.kyrohpaneup.abirechner.activities

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.lifecycle.ViewModelProvider
import de.kyrohpaneup.abirechner.R
import de.kyrohpaneup.abirechner.data.database.AppDatabase
import de.kyrohpaneup.abirechner.data.GradeManager
import de.kyrohpaneup.abirechner.data.database.Grade
import de.kyrohpaneup.abirechner.data.viewmodels.HeadGradeViewModel
import de.kyrohpaneup.abirechner.data.viewmodels.HeadGradeViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.kyrohpaneup.abirechner.adapters.GradeAdapter
import de.kyrohpaneup.abirechner.data.utils.DoubleIDClass
import de.kyrohpaneup.abirechner.data.utils.StringIDClass

class HeadGradeActivity : ComponentActivity() {

    private lateinit var listView: ListView
    private lateinit var subjectView: AutoCompleteTextView
    private lateinit var teacherView: EditText
    private lateinit var yearView: AutoCompleteTextView


    private lateinit var addGradeButton: Button
    private lateinit var saveButton: Button
    private lateinit var deleteButton: Button

    private lateinit var gradeNumberView: TextView
    private lateinit var gradePointsView: TextView

    private lateinit var viewModel: HeadGradeViewModel

    private val grades: MutableList<Grade> = mutableListOf()
    private lateinit var arrayAdapter: GradeAdapter

    private var headGradeId: String = ""
    private var selectedYearId: Double? = null
    private var selectedSubjectId: String? = null
    private var gradeManager = GradeManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.headgrade_activity_layout)

        onBackPressedDispatcher.addCallback(this) {
            goToParent()
        }

        bindViews()
        setupList()

        headGradeId = intent.getStringExtra("headGradeId") ?: ""

        val db = AppDatabase.getDatabase(this)
        val dao = db.gradeDao()
        val subjectDao = db.subjectDao()
        val factory = HeadGradeViewModelFactory(dao, subjectDao)
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

        val yearOptions = mutableListOf<DoubleIDClass>()
        for (i in 13 downTo 1) {
            yearOptions.add(DoubleIDClass(i + 0.2, gradeManager.getYearFromId(i + 0.2)))
            yearOptions.add(DoubleIDClass(i + 0.1, gradeManager.getYearFromId(i + 0.1)))
        }

        val yearAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            yearOptions
        )
        yearView.setAdapter(yearAdapter)
    }

    private fun observeViewModel() {
        viewModel.headGrade.observe(this) { grade ->
            headGradeId = viewModel.getHeadGradeId()

            subjectView.setText(grade.subject)
            teacherView.setText(grade.teacher)
            yearView.setText(gradeManager.getYearFromId(grade.year))

            val points = grade.grade ?: 0
            updateGradeUI(points)
        }

        viewModel.childGrades.observe(this) { list ->
            grades.clear()
            grades.addAll(list)
            arrayAdapter.notifyDataSetChanged()
        }

        viewModel.subject.observe(this) { subject ->
            subjectView.setText(subject.name)
        }

        viewModel.subjects.observe(this) { subjects ->
            val subjectOptions = mutableListOf<StringIDClass>()
            for (subject in subjects) {
                if (subject.name != null) {
                    subjectOptions.add(StringIDClass(subject.id, subject.name!!))
                }
            }

            val subjectAdapter = ArrayAdapter(
                this,
                android.R.layout.simple_list_item_1,
                subjectOptions
            )
            subjectView.setAdapter(subjectAdapter)
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
                selectedSubjectId,
                teacherView.text.toString(),
                selectedYearId
            )

            goToParent()
        }

        deleteButton.setOnClickListener {
            viewModel.deleteHead()
            goToParent()
        }

        yearView.setOnClickListener {
            yearView.showDropDown()
        }

        yearView.setOnItemClickListener { parent, _, position, _ ->
            val selectedItem = parent.getItemAtPosition(position) as DoubleIDClass
            selectedYearId = selectedItem.id
        }

        subjectView.setOnClickListener {
            subjectView.showDropDown()
        }

        subjectView.setOnItemClickListener { parent, _, position, _ ->
            val selectedItem = parent.getItemAtPosition(position) as StringIDClass
            selectedSubjectId = selectedItem.id
        }
    }

    private fun updateGradeUI(points: Int) {
        val manager = GradeManager()
        gradeNumberView.text = manager.getNumberForPoints(points)
        gradePointsView.text = points.toString()
    }

    private fun openGradeActivity(grade: Grade) {
        val intent: Intent = if (grade.isCalculated) {
            Intent(this, ParentGradeActivity::class.java)
        } else {
            Intent(this, ChildGradeActivity::class.java)
        }
        intent.putExtra("gradeId", grade.id)
        startActivity(intent)
    }

    private fun goToParent() {
        finish()
        val intent = Intent(this, GradeActivity::class.java)
        startActivity(intent)
    }
}