package de.kyrohpaneup.abirechner.activities

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.kyrohpaneup.abirechner.R
import de.kyrohpaneup.abirechner.adapters.GradeAdapter
import de.kyrohpaneup.abirechner.data.database.AppDatabase
import de.kyrohpaneup.abirechner.data.GradeManager
import de.kyrohpaneup.abirechner.data.database.Grade
import de.kyrohpaneup.abirechner.data.viewmodels.ParentGradeViewModel
import de.kyrohpaneup.abirechner.data.viewmodels.ParentGradeViewModelFactory

class ParentGradeActivity : ComponentActivity() {

    private lateinit var titleView: EditText
    private lateinit var weightView: TextView
    private lateinit var weightBar: SeekBar
    private lateinit var addGradeButton: Button
    private lateinit var saveButton: Button
    private lateinit var deleteButton: Button
    private lateinit var listView: ListView
    private lateinit var gradeNumberView: TextView
    private lateinit var gradePointsView: TextView

    private lateinit var viewModel: ParentGradeViewModel

    private val grades: MutableList<Grade> = mutableListOf()
    private lateinit var arrayAdapter: GradeAdapter

    private var gradeId: String = ""
    private var headGradeId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.parent_grade_layout)

        onBackPressedDispatcher.addCallback(this) {
            goToParent()
        }

        bindViews()
        setupList()

        gradeId = intent.getStringExtra("gradeId") ?: ""

        val dao = AppDatabase.getDatabase(this).gradeDao()
        val factory = ParentGradeViewModelFactory(dao)
        viewModel = ViewModelProvider(this, factory)[ParentGradeViewModel::class]

        observeViewModel()
        setupListeners()

        viewModel.loadParent(gradeId)
    }

    private fun bindViews() {
        titleView = findViewById(R.id.title_text)
        weightView = findViewById(R.id.weight_text)
        weightBar = findViewById(R.id.weight_bar)
        addGradeButton = findViewById(R.id.add_grade_button)
        saveButton = findViewById(R.id.save_button)
        deleteButton = findViewById(R.id.delete_button)
        listView = findViewById(R.id.list_view)
        gradeNumberView = findViewById(R.id.grade_number_view)
        gradePointsView = findViewById(R.id.grade_points_view)
    }

    private fun setupList() {
        arrayAdapter = GradeAdapter(this, grades)
        listView.adapter = arrayAdapter

        listView.setOnItemClickListener { _, _, position, _ ->
            val clicked = grades[position]
            updateParent()
            openGradeActivity(clicked)
        }
    }

    private fun observeViewModel() {
        viewModel.parentGrade.observe(this) { grade ->
            headGradeId = viewModel.getHeadGradeId()

            titleView.setText(grade.name)

            val weight = grade.weight ?: 0
            weightBar.progress = weight
            weightView.text = "Weight $weight%"

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

        weightBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                weightView.text = "Weight $progress%"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        saveButton.setOnClickListener {
            updateParent()
            goToParent()
        }

        deleteButton.setOnClickListener {
            viewModel.deleteParent()
            goToParent()
        }
    }

    private fun updateParent() {
        viewModel.updateParent(
            titleView.text.toString(),
            weightBar.progress
        )
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
        lateinit var intent: Intent
        val grade = viewModel.parentGrade.value
        if (grade?.parentGrade == grade?.headGrade) {
            intent = Intent(this, HeadGradeActivity::class.java)
            intent.putExtra("headGradeId", headGradeId)
        } else {
            intent = Intent(this, ParentGradeActivity::class.java)
            intent.putExtra("gradeId", grade?.parentGrade)
        }
        startActivity(intent)
        finish()
    }
}