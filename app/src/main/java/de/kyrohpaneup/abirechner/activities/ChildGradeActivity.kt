package de.kyrohpaneup.abirechner.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.ComponentActivity
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ChildGradeActivity : AppCompatActivity() {

    private lateinit var titleView: EditText
    private lateinit var notesView: EditText
    private lateinit var ignoreGradeBox: CheckBox
    private lateinit var datePicker: DatePicker
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

        // Fix: Show DatePickerDialog when clicking on the DatePicker
        datePicker.setOnClickListener {
            showDatePickerDialog()
        }
    }

    private fun bindViews() {
        titleView = findViewById(R.id.title_text)
        notesView = findViewById(R.id.notes_text)
        ignoreGradeBox = findViewById(R.id.ignore_grade_checkbox)
        datePicker = findViewById(R.id.date_picker)
        weightText = findViewById(R.id.weight_text)
        weightBar = findViewById(R.id.weight_bar)
        gradeSpinner = findViewById(R.id.set_grade_spinner)
        gradeNumberView = findViewById(R.id.grade_number_view)
        gradePointsView = findViewById(R.id.grade_points_view)
        saveButton = findViewById(R.id.save_button)
        deleteButton = findViewById(R.id.delete_button)
        datePicker.setDescendantFocusability(DatePicker.FOCUS_BLOCK_DESCENDANTS)
    }

    private fun observeViewModel() {
        viewModel.grade.observe(this) { g ->
            grade = g

            titleView.setText(g.name)
            notesView.setText(g.notes)
            ignoreGradeBox.isChecked = g.ignoreGrade == true
            setDatePickerFromString()

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

    // Fix: Renamed from showDatePicker to showDatePickerDialog
    private fun showDatePickerDialog() {
        // Get current date from DatePicker or use today
        val calendar = Calendar.getInstance().apply {
            set(datePicker.year, datePicker.month, datePicker.dayOfMonth)
        }

        // Create Material DatePicker
        val materialDatePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select Date")
            .setSelection(calendar.timeInMillis)
            .build()

        materialDatePicker.addOnPositiveButtonClickListener { selection ->
            // selection is a Long timestamp
            val date = Date(selection)
            val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

            // Update the DatePicker with the selected date
            val selectedCalendar = Calendar.getInstance().apply {
                time = date
            }
            datePicker.updateDate(
                selectedCalendar.get(Calendar.YEAR),
                selectedCalendar.get(Calendar.MONTH),
                selectedCalendar.get(Calendar.DAY_OF_MONTH)
            )
        }

        materialDatePicker.show(supportFragmentManager, "DATE_PICKER")
    }

    private fun setDatePickerFromString(pattern: String = "dd/MM/yyyy") {
        try {
            if (grade?.date == null) return
            val dateFormat = SimpleDateFormat(pattern, Locale.getDefault())
            val date = dateFormat.parse(grade?.date!!) ?: return

            val calendar = Calendar.getInstance().apply {
                time = date
            }

            datePicker.updateDate(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
        } catch (e: Exception) {
            val calendar = Calendar.getInstance()
            datePicker.updateDate(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
        }
    }

    private fun saveGrade() {
        val day = datePicker.dayOfMonth
        val month = datePicker.month // Note: month is 0-based (0-11)
        val year = datePicker.year
        val calendar = Calendar.getInstance()
        calendar.set(year, month, day)

        val dateString = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.time)

        grade?.let {
            it.name = titleView.text.toString()
            it.notes = notesView.text.toString()
            it.ignoreGrade = ignoreGradeBox.isChecked
            it.date = dateString

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