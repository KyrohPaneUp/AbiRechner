package de.kyrohpaneup.abirechner.activities

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import de.kyrohpaneup.abirechner.R
import de.kyrohpaneup.abirechner.data.database.AppDatabase
import de.kyrohpaneup.abirechner.data.GradeManager
import de.kyrohpaneup.abirechner.data.database.Grade
import de.kyrohpaneup.abirechner.data.viewmodels.HeadGradeViewModel
import de.kyrohpaneup.abirechner.data.viewmodels.HeadGradeViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import de.kyrohpaneup.abirechner.adapters.GradeAdapter
import de.kyrohpaneup.abirechner.data.GradeGraphResult
import de.kyrohpaneup.abirechner.data.utils.DoubleIDClass
import de.kyrohpaneup.abirechner.utils.Constant
import java.util.Locale
import kotlin.text.*

class HeadGradeActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var subjectView: TextView
    private lateinit var teacherView: EditText
    private lateinit var yearView: MaterialAutoCompleteTextView


    private lateinit var addGradeButton: Button
    private lateinit var saveButton: Button
    private lateinit var deleteButton: Button

    private lateinit var gradeNumberView: TextView
    private lateinit var gradePointsView: TextView

    private lateinit var gradeChart: LineChart

    private lateinit var viewModel: HeadGradeViewModel

    private val grades: MutableList<Grade> = mutableListOf()
    private lateinit var arrayAdapter: GradeAdapter

    private var headGradeId: String = ""
    private var selectedYearId: Double? = null
    private var gradeManager = GradeManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.headgrade_activity_layout)

        onBackPressedDispatcher.addCallback(this) {
            goToParent()
        }

        bindViews()
        setupList()

        headGradeId = intent.getStringExtra(Constant.HEADGRADE_ID) ?: ""

        val db = AppDatabase.getDatabase(this)
        val dao = db.gradeDao()
        val subjectDao = db.subjectDao()
        val factory = HeadGradeViewModelFactory(dao, subjectDao)
        viewModel = ViewModelProvider(this, factory)[HeadGradeViewModel::class]

        observeViewModel()
        setupListeners()

        viewModel.loadHead(headGradeId)
    }

    private fun bindViews() {
        this.subjectView = findViewById(R.id.subjectText)
        this.teacherView = findViewById(R.id.teacher_text)
        this.yearView = findViewById(R.id.year_text)

        this.addGradeButton = findViewById(R.id.addGradeButton)
        this.saveButton = findViewById(R.id.save_button)
        this.deleteButton = findViewById(R.id.delete_button)

        this.listView = findViewById(R.id.listView)

        this.gradeNumberView = findViewById(R.id.grade_number_view)
        this.gradePointsView = findViewById(R.id.grade_points_view)

        this.gradeChart = findViewById(R.id.grade_chart)
    }

    private fun setupList() {
        arrayAdapter = GradeAdapter(this, grades)
        listView.adapter = arrayAdapter

        listView.setOnItemClickListener { _, _, position, _ ->
            val clicked = grades[position]
            updateHead()
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

            teacherView.setText(grade.teacher)
            yearView.setText(gradeManager.getYearFromId(grade.year), false)

            val points = grade.grade ?: 0
            updateGradeUI(points)
        }

        viewModel.childGrades.observe(this) { list ->
            grades.clear()
            grades.addAll(list)
            arrayAdapter.notifyDataSetChanged()
        }

        viewModel.allGrades.observe(this) { list ->
            loadChart(gradeManager.calculateGradeGraph(viewModel.getHeadGradeId(), list))
        }

        viewModel.subject.observe(this) { subject ->
            subjectView.text = subject.name
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
            updateHead()

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
    }

    private fun updateHead() {
        viewModel.updateHead(
            teacherView.text.toString(),
            selectedYearId
        )
    }

    private fun updateGradeUI(points: Int) {
        val manager = GradeManager()
        gradeNumberView.text = manager.getNumberForPoints(points)
        gradePointsView.text = points.toString()
    }

    private fun loadChart(dataList: List<GradeGraphResult>) {
        if (dataList.isEmpty()) {
            gradeChart.clear()
            gradeChart.invalidate()
            return
        }

        gradeChart.clear()

        gradeChart.description.isEnabled = false
        gradeChart.setTouchEnabled(true)
        gradeChart.isDragEnabled = true
        gradeChart.setScaleEnabled(true)
        gradeChart.setPinchZoom(true)
        gradeChart.setDrawGridBackground(false)
        gradeChart.setBackgroundColor(android.graphics.Color.TRANSPARENT)

        gradeChart.setExtraOffsets(10f, 25f, 10f, 10f)

        val xAxis = gradeChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.setDrawLabels(true)
        xAxis.setDrawGridLines(true)
        xAxis.textSize = 10f
        xAxis.textColor = android.graphics.Color.WHITE
        xAxis.gridColor = android.graphics.Color.DKGRAY

        val leftAxis = gradeChart.axisLeft
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = 16f
        leftAxis.granularity = 1f
        leftAxis.setDrawLabels(true)
        leftAxis.setDrawGridLines(true)
        leftAxis.textSize = 10f
        leftAxis.textColor = android.graphics.Color.WHITE
        leftAxis.gridColor = android.graphics.Color.DKGRAY
        leftAxis.axisLineColor = android.graphics.Color.WHITE

        leftAxis.spaceTop = 15f
        leftAxis.spaceBottom = 5f

        gradeChart.axisRight.isEnabled = false

        val legend = gradeChart.legend
        legend.isEnabled = true
        legend.textSize = 12f
        legend.textColor = android.graphics.Color.WHITE

        val grades: ArrayList<Entry> = ArrayList()
        val dateLabels = ArrayList<String>()

        for ((index, data) in dataList.withIndex()) {
            val entry = Entry(index.toFloat(), data.y.toFloat())
            grades.add(entry)

            val date = java.text.SimpleDateFormat("dd.MM.yy", Locale.getDefault())
                .format(java.util.Date(data.x))
            dateLabels.add(date)
        }

        xAxis.valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val index = value.toInt()
                return if (index >= 0 && index < dateLabels.size) {
                    dateLabels[index]
                } else {
                    ""
                }
            }
        }

        val lineDataSet = LineDataSet(grades, "Notenentwicklung")
        lineDataSet.setDrawCircles(true)
        lineDataSet.circleRadius = 8f
        lineDataSet.circleHoleRadius = 4f
        lineDataSet.setDrawValues(true)
        lineDataSet.valueTextSize = 11f
        lineDataSet.valueTextColor = android.graphics.Color.WHITE

        lineDataSet.valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return String().format(Locale.getDefault(), value)
            }
        }

        lineDataSet.lineWidth = 3f
        lineDataSet.setColor(android.graphics.Color.GREEN)
        lineDataSet.setCircleColor(android.graphics.Color.RED)
        lineDataSet.circleHoleRadius = android.graphics.Color.WHITE.toFloat()
        lineDataSet.mode = LineDataSet.Mode.LINEAR

        val dataSets = ArrayList<ILineDataSet>()
        dataSets.add(lineDataSet)

        val lineData = LineData(dataSets)
        gradeChart.data = lineData

        gradeChart.invalidate()
        gradeChart.animateX(1000)
    }

    private fun openGradeActivity(grade: Grade) {
        val intent: Intent = if (grade.isCalculated) {
            Intent(this, ParentGradeActivity::class.java)
        } else {
            Intent(this, ChildGradeActivity::class.java)
        }
        intent.putExtra(Constant.GRADE_ID, grade.id)
        startActivity(intent)
    }

    private fun goToParent() {
        finish()
        val intent = Intent(this, SubjectActivity::class.java)
        intent.putExtra(Constant.SUBJECT_ID, viewModel.subject.value?.id)
        startActivity(intent)
    }
}