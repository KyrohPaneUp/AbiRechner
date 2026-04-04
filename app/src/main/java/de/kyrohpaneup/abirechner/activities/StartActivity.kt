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
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import de.kyrohpaneup.abirechner.R
import de.kyrohpaneup.abirechner.adapters.SubjectAdapter
import de.kyrohpaneup.abirechner.data.GradeManager
import de.kyrohpaneup.abirechner.data.database.AppDatabase
import de.kyrohpaneup.abirechner.data.database.Subject
import de.kyrohpaneup.abirechner.data.viewmodels.SubjectViewModel
import de.kyrohpaneup.abirechner.data.viewmodels.SubjectViewModelFactory
import de.kyrohpaneup.abirechner.utils.Constant

class StartActivity : AppCompatActivity() {
    private var subjects: MutableList<Subject> = mutableListOf()
    private var context: Context = this
    private lateinit var subjectButton: Button
    private lateinit var subjectListView: ListView
    private lateinit var subjectAdapter: SubjectAdapter

    private lateinit var e1Value: TextView
    private lateinit var e2Value: TextView
    private lateinit var totalValue: TextView
    private lateinit var gradeValue: TextView

    private lateinit var viewModel: SubjectViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.start_activity_layout)

        bindViews()
        setupList()

        val db = AppDatabase.getDatabase(this)
        val gradeDao = db.gradeDao()
        val subjectDao = db.subjectDao()
        val factory = SubjectViewModelFactory(gradeDao, subjectDao)
        viewModel = ViewModelProvider(this, factory)[SubjectViewModel::class]

        viewModel.loadData()

        observeViewModel()
        setupListeners()
    }

    private fun bindViews() {
        subjectListView = findViewById(R.id.subject_list_view)
        subjectButton = findViewById(R.id.subject_button)

        e1Value = findViewById(R.id.e1_value)
        e2Value = findViewById(R.id.e2_value)
        totalValue = findViewById(R.id.total_value)
        gradeValue = findViewById(R.id.grade_value)
    }

    private fun setupList() {
        subjectAdapter = SubjectAdapter(context, subjects)
        subjectListView.adapter = subjectAdapter

        subjectListView.setOnItemClickListener { _, _, position, _ ->
            val clickedSubject = subjects[position]
            openGradeActivity(clickedSubject)
        }
    }

    private fun observeViewModel() {
        val gradeManager = GradeManager()

        viewModel.subjects.observe(this) { list ->
            subjects.clear()
            subjects.addAll(list)
            subjectAdapter.notifyDataSetChanged()
        }

        viewModel.heads.observe(this) { list ->
            val e1 = gradeManager.calculateE1Grade(subjects, list)
            val e2 = gradeManager.calculateE2Grade(subjects)
            val total = e1 + e2
            val average = gradeManager.calculateAverageFromPoints(total) ?: "N/A"
            "$e1/600".also { e1Value.text = it }
            "$e2/300".also { e2Value.text = it }
            "$total/900".also { totalValue.text = it }
            "$average".also { gradeValue.text = it }
        }
    }

    private fun setupListeners() {
        subjectButton.setOnClickListener {
            showTextInputDialog()
        }
    }

    private fun openGradeActivity(subject: Subject) {
        val intent = Intent(this, SubjectActivity::class.java)
        intent.putExtra(Constant.SUBJECT_ID, subject.id)
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
