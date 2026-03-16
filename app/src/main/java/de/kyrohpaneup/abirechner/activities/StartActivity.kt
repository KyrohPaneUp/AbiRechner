package de.kyrohpaneup.abirechner.activities

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import de.kyrohpaneup.abirechner.R
import de.kyrohpaneup.abirechner.adapters.SubjectAdapter
import de.kyrohpaneup.abirechner.data.database.AppDatabase
import de.kyrohpaneup.abirechner.data.database.Subject
import de.kyrohpaneup.abirechner.data.database.dao.SubjectDao
import de.kyrohpaneup.abirechner.data.viewmodels.SubjectViewModel
import de.kyrohpaneup.abirechner.data.viewmodels.SubjectViewModelFactory
import de.kyrohpaneup.abirechner.utils.Constant

class StartActivity : AppCompatActivity() {
    private var subjects: MutableList<Subject> = mutableListOf()
    private var context: Context = this
    private lateinit var subjectButton: Button
    private lateinit var subjectListView: ListView
    private lateinit var subjectAdapter: SubjectAdapter


    private lateinit var viewModel: SubjectViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.start_activity_layout)

        bindViews()
        setupList()

        val subjectDao: SubjectDao = AppDatabase.getDatabase(this).subjectDao()
        val factory = SubjectViewModelFactory(subjectDao)
        viewModel = ViewModelProvider(this, factory)[SubjectViewModel::class]

        viewModel.loadData()

        observeViewModel()
        setupListeners()
    }

    private fun bindViews() {
        subjectListView = findViewById(R.id.subject_list_view)
        subjectButton = findViewById(R.id.subject_button)
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
        viewModel.subjects.observe(this) { list ->
            subjects.clear()
            subjects.addAll(list)
            subjectAdapter.notifyDataSetChanged()
        }
    }

    private fun setupListeners() {
        subjectButton.setOnClickListener() {
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
