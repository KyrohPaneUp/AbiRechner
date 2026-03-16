package de.kyrohpaneup.abirechner.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.datepicker.MaterialDatePicker
import de.kyrohpaneup.abirechner.R
import de.kyrohpaneup.abirechner.data.database.AppDatabase
import de.kyrohpaneup.abirechner.data.GradeManager
import de.kyrohpaneup.abirechner.data.database.Grade
import de.kyrohpaneup.abirechner.data.viewmodels.ChildGradeViewModel
import de.kyrohpaneup.abirechner.data.viewmodels.ChildGradeViewModelFactory
import de.kyrohpaneup.abirechner.utils.Constant
import java.text.SimpleDateFormat
import java.util.*
import java.util.Calendar

class ChildGradeActivity : AppCompatActivity() {

    private lateinit var titleView: EditText
    private lateinit var notesView: EditText
    private lateinit var ignoreGradeBox: CheckBox
    private lateinit var dateText: TextView
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

    private val displayFormat = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
    private val storageFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

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

        gradeId = intent.getStringExtra(Constant.GRADE_ID) ?: ""

        setupWeightBar()
        setupSpinner()
        observeViewModel()

        viewModel.loadGrade(gradeId)

        saveButton.setOnClickListener { saveGrade() }

        deleteButton.setOnClickListener {
            viewModel.deleteGrade()
            goToParent()
        }

        dateText.setOnClickListener {
            showDatePickerDialog()
        }
    }

    private fun bindViews() {
        titleView = findViewById(R.id.title_text)
        notesView = findViewById(R.id.notes_text)
        ignoreGradeBox = findViewById(R.id.ignore_grade_checkbox)
        dateText = findViewById(R.id.date_text)
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

            // Format date for display if it exists
            g.date?.let { dateMillis ->
                dateText.text = formatDateForDisplay(dateMillis)
            } ?: run {
                dateText.text = ""
            }

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

    private fun showDatePickerDialog() {
        val initialMillis = grade?.date ?: System.currentTimeMillis()

        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select Date")
            .setSelection(initialMillis)
            .build()

        picker.addOnPositiveButtonClickListener { selection ->
            grade?.date = selection
            dateText.text = formatDateForDisplay(selection)
        }

        picker.show(supportFragmentManager, "DATE_PICKER")
    }

    private fun formatDateForDisplay(dateMillis: Long): String {
        return try {
            val calendar = Calendar.getInstance().apply { timeInMillis = dateMillis }
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val month = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())
            val year = calendar.get(Calendar.YEAR)
            "$month ${getDayWithSuffix(day)}, $year"
        } catch (e: Exception) {
            ""
        }
    }

    private fun getDayWithSuffix(day: Int): String {
        return when (day) {
            1, 21, 31 -> "${day}st"
            2, 22 -> "${day}nd"
            3, 23 -> "${day}rd"
            else -> "${day}th"
        }
    }

    private fun saveGrade() {
        grade?.let { g ->
            g.name = titleView.text.toString()
            g.notes = notesView.text.toString()
            g.ignoreGrade = ignoreGradeBox.isChecked

            // Set current date if no date is set
            if (g.date == null) {
                g.date = System.currentTimeMillis()
            }

            viewModel.updateGrade(g)
            goToParent()
        }
    }

    private fun goToParent() {
        val grade = viewModel.grade.value
        val intent = if (grade?.parentGrade == grade?.headGrade) {
            Intent(this, HeadGradeActivity::class.java).apply {
                putExtra(Constant.HEADGRADE_ID, grade?.headGrade)
            }
        } else {
            Intent(this, ParentGradeActivity::class.java).apply {
                putExtra(Constant.GRADE_ID, grade?.parentGrade)
            }
        }
        startActivity(intent)
        finish()
    }
}