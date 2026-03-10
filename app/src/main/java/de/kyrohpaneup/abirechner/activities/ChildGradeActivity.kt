package de.kyrohpaneup.abirechner.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.lifecycle.ViewModelProvider
import de.kyrohpaneup.abirechner.R
import de.kyrohpaneup.abirechner.data.database.AppDatabase
import de.kyrohpaneup.abirechner.data.GradeManager
import de.kyrohpaneup.abirechner.data.database.Grade
import de.kyrohpaneup.abirechner.data.viewmodels.ChildGradeViewModel
import de.kyrohpaneup.abirechner.data.viewmodels.ChildGradeViewModelFactory

class ChildGradeActivity : ComponentActivity() {

    private lateinit var titleView: EditText
    private lateinit var notesView: EditText
    private lateinit var ignoreGradeBox: CheckBox
    private lateinit var dateView: EditText
    private lateinit var weightText: TextView
    private lateinit var weightBar: SeekBar
    private lateinit var gradeSpinner: Spinner
    private lateinit var gradeNumberView: TextView
    private lateinit var gradePointsView: TextView
    private lateinit var saveButton: Button
    private lateinit var deleteButton: Button

    private lateinit var viewModel: ChildGradeViewModel
    private var grade: Grade? = null
    private var gradeId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.child_grade_layout)

        onBackPressedDispatcher.addCallback(this) {
            goToParent()
        }

        bindViews()

        val dao = AppDatabase.getDatabase(this).gradeDao()
        val factory = ChildGradeViewModelFactory(dao)
        viewModel = ViewModelProvider(this, factory)[ChildGradeViewModel::class]

        gradeId = intent.getStringExtra("gradeId") ?: ""

        setupWeightBar()
        setupSpinner()
        observeViewModel()

        viewModel.loadGrade(gradeId)

        saveButton.setOnClickListener { saveGrade() }
    }

    private fun bindViews() {
        titleView = findViewById(R.id.title_text)
        notesView = findViewById(R.id.notes_text)
        ignoreGradeBox = findViewById(R.id.ignore_grade_checkbox)
        dateView = findViewById(R.id.date_text)
        weightText = findViewById(R.id.weight_text)
        weightBar = findViewById(R.id.weight_bar)
        gradeSpinner = findViewById(R.id.set_grade_spinner)
        gradeNumberView = findViewById(R.id.grade_number_view)
        gradePointsView = findViewById(R.id.grade_points_view)
        saveButton = findViewById(R.id.save_button)
        deleteButton = findViewById(R.id.delete_button)
    }

    private fun observeViewModel() {
        viewModel.grade.observe(this) { g ->
            grade = g

            titleView.setText(g.name)
            notesView.setText(g.notes)
            ignoreGradeBox.isChecked = g.ignoreGrade == true
            dateView.setText(g.date)

            val weight = g.weight ?: 0
            weightBar.progress = weight
            weightText.text = "Weight $weight%"

            val points = g.grade ?: 0
            updateGradeUI(points)
            gradeSpinner.setSelection(points)
        }
    }

    private fun setupWeightBar() {
        weightBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                weightText.text = "Weight $progress%"
                grade?.weight = progress
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun setupSpinner() {
        val pointLabels = (0..15).map { "$it P." }
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            pointLabels
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        gradeSpinner.adapter = adapter

        gradeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                grade?.grade = pos
                updateGradeUI(pos)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun updateGradeUI(points: Int) {
        val manager = GradeManager()
        gradeNumberView.text = manager.getNumberForPoints(points)
        gradePointsView.text = points.toString()
    }

    private fun saveGrade() {
        grade?.let {
            it.name = titleView.text.toString()
            it.notes = notesView.text.toString()
            it.ignoreGrade = ignoreGradeBox.isChecked
            it.date = dateView.text.toString()

            viewModel.updateGrade(it)
            goToParent()
        }
    }

    private fun goToParent() {
        lateinit var intent: Intent
        val grade = viewModel.grade.value
        if (grade?.parentGrade == grade?.headGrade) {
            intent = Intent(this, HeadGradeActivity::class.java)
            intent.putExtra("headGradeId", grade?.headGrade)
        } else {
            intent = Intent(this, ParentGradeActivity::class.java)
            intent.putExtra("gradeId", grade?.parentGrade)
        }
        startActivity(intent)
        finish()
    }
}