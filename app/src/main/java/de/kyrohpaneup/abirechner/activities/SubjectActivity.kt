package de.kyrohpaneup.abirechner.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.model.GradientColor
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


class SubjectActivity : AppCompatActivity() {
    private var grades: MutableList<HeadGrade> = mutableListOf()
    private var context: Context = this
    private lateinit var subjectText: TextView
    private lateinit var gradeButton: Button
    private lateinit var gradeListView: ListView
    private lateinit var gradeAdapter: HeadGradeAdapter
    private lateinit var chart: LineChart

    private lateinit var viewModel: GradeViewModel



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
        gradeButton = findViewById(de.kyrohpaneup.abirechner.R.id.grade_button)
        chart = findViewById(de.kyrohpaneup.abirechner.R.id.grade_chart)
    }

    private fun setupList() {
        gradeAdapter = HeadGradeAdapter(context, grades)  { viewModel.getSubjectName() }
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
    }

    private fun setupListeners() {
        gradeButton.setOnClickListener {
            viewModel.addHeadGrade()
        }
    }

    private fun loadChart(dataList: List<GradeGraphResult>) {
        val dataSets: ArrayList<ILineDataSet> = ArrayList()
        val grades: ArrayList<Entry> = ArrayList()
        for (data in dataList) {
            val entry = Entry(data.x.toFloat(), data.y.toFloat())
            grades.add(entry)
        }

        val lineDataSet: LineDataSet = LineDataSet(grades, "Grade")
        lineDataSet.setDrawCircles(true)
        lineDataSet.circleRadius = 4.0f
        lineDataSet.setDrawValues(false)
        lineDataSet.lineWidth = 3.0f
        lineDataSet.setColor(Color.Green.value.toInt())
        lineDataSet.setCircleColor(Color.Red.value.toInt())
        dataSets.add(lineDataSet)

        val lineData = LineData(dataSets)
        chart.data = lineData
        chart.invalidate()
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
}
