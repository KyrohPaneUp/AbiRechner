package de.kyrohpaneup.abirechner.activities

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
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
import de.kyrohpaneup.abirechner.adapters.HeadGradeAdapter
import de.kyrohpaneup.abirechner.data.GradeGraphResult
import de.kyrohpaneup.abirechner.data.GradeManager
import de.kyrohpaneup.abirechner.data.database.AppDatabase
import de.kyrohpaneup.abirechner.data.database.HeadGrade
import de.kyrohpaneup.abirechner.data.database.dao.GradeDao
import de.kyrohpaneup.abirechner.data.database.dao.SubjectDao
import de.kyrohpaneup.abirechner.data.viewmodels.GradeViewModel
import de.kyrohpaneup.abirechner.data.viewmodels.GradeViewModelFactory
import de.kyrohpaneup.abirechner.utils.Constant
import java.util.Locale


class SubjectActivity : AppCompatActivity() {
    private var grades: MutableList<HeadGrade> = mutableListOf()
    private var context: Context = this
    private lateinit var subjectText: TextView
    private lateinit var gradeButton: Button
    private lateinit var saveButton: Button
    private lateinit var deleteButton: Button
    private lateinit var gradeListView: ListView
    private lateinit var gradeAdapter: HeadGradeAdapter
    private lateinit var gradeChart: LineChart

    private lateinit var viewModel: GradeViewModel

    private val gradeManager = GradeManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.subject_activity_layout)

        onBackPressedDispatcher.addCallback(this) {
            goToParent()
        }

        bindViews()
        setupList()

        val gradeDao: GradeDao = AppDatabase.getDatabase(this).gradeDao()
        val subjectDao: SubjectDao = AppDatabase.getDatabase(this).subjectDao()
        val factory = GradeViewModelFactory(gradeDao, subjectDao)
        viewModel = ViewModelProvider(this, factory)[GradeViewModel::class]

        val subjectId = intent.getStringExtra(Constant.SUBJECT_ID) ?: ""
        viewModel.loadData(subjectId)

        observeViewModel()
        setupListeners()
    }

    private fun bindViews() {
        subjectText = findViewById(R.id.subject_text)
        gradeListView = findViewById(R.id.grade_list_view)
        gradeButton = findViewById(R.id.grade_button)
        gradeChart = findViewById(R.id.grade_chart)
        saveButton = findViewById(R.id.save_button)
        deleteButton = findViewById(R.id.delete_button)
    }

    private fun setupList() {
        gradeAdapter = HeadGradeAdapter(context, grades)
        gradeListView.adapter = gradeAdapter

        gradeListView.setOnItemClickListener { _, _, position, _ ->
            val clickedHeadGrade = grades[position]
            openHeadGradeActivity(clickedHeadGrade)
        }
    }

    private fun observeViewModel() {
        viewModel.subject.observe(this) { subject ->
            subjectText.text = subject.name
        }

        viewModel.grades.observe(this) { list ->
            grades.clear()
            grades.addAll(list)
            gradeAdapter.notifyDataSetChanged()
        }

        viewModel.allGrades.observe(this) { list ->
            val headById = viewModel.grades.value?.filter { true }?.associate { it.id to it }
            val gradesByHead = list.filter { it.headGrade != null }.groupBy { it.headGrade!! }.toMap()
            if (headById == null) return@observe

            val data = gradeManager.calculateSubjectGraph(headById, gradesByHead)
            if (data.size < 2) return@observe
            loadChart(data)
        }
    }

    private fun setupListeners() {
        subjectText.setOnClickListener {
            showTextInputDialog()
        }

        gradeButton.setOnClickListener {
            viewModel.addHeadGrade()
        }

        saveButton.setOnClickListener {
            viewModel.updateSubject(subjectText.text.toString())
            goToParent()
        }

        deleteButton.setOnClickListener {
            viewModel.deleteSubject()
            goToParent()
        }
    }

    private fun showTextInputDialog() {
        val input = EditText(this)
        input.hint = "Enter name here"

        AlertDialog.Builder(this)
            .setTitle("Enter new Name")
            .setMessage("Please enter the new subject name:")
            .setView(input)
            .setPositiveButton("Enter") { dialog, _ ->
                val text = input.text.toString()
                if (text.isNotEmpty()) {
                    subjectText.text = text
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

    private fun openHeadGradeActivity(headGrade: HeadGrade) {
        val intent = Intent(this, HeadGradeActivity::class.java)
        intent.putExtra(Constant.HEADGRADE_ID, headGrade.id)
        startActivity(intent)
    }

    private fun goToParent() {
        finish()
        val intent = Intent(this, StartActivity::class.java)
        startActivity(intent)
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

            var timestamp = gradeManager.getYearFromId(data.x.toDouble())
            timestamp = timestamp.replace("Year ", "y")
            timestamp = timestamp.replace("semester ", "s")
            dateLabels.add(timestamp)
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
}
