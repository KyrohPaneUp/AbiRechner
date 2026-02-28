package de.kyrohpaneup.abirechner.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.lifecycle.ViewModelProvider
import de.kyrohpaneup.abirechner.R
import de.kyrohpaneup.abirechner.data.AppDatabase
import de.kyrohpaneup.abirechner.data.grades.GradeDao
import de.kyrohpaneup.abirechner.data.grades.HeadGrade
import de.kyrohpaneup.abirechner.data.viewmodels.GradeViewModel
import de.kyrohpaneup.abirechner.data.viewmodels.GradeViewModelFactory
import de.kyrohpaneup.abirechner.ui.theme.AbiRechnerTheme

class GradeActivity : ComponentActivity() {
    private var grades: MutableList<HeadGrade> = mutableListOf()
    private lateinit var listView: ListView
    private var context: Context = this
    private lateinit var button: Button
    private lateinit var arrayAdapter: HeadGradeAdapter

    private lateinit var viewModel: GradeViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.main_activity_layout)

        bindViews()
        setupList()

        val dao: GradeDao = AppDatabase.getDatabase(this).gradeDao()
        val factory = GradeViewModelFactory(dao)
        viewModel = ViewModelProvider(this, factory)[GradeViewModel::class]

        viewModel.loadGrades()

        observeViewModel()
        setupListeners()
    }

    private fun bindViews() {
        listView = findViewById(R.id.listView)
        button = findViewById(R.id.button)
    }

    private fun setupList() {
        arrayAdapter = HeadGradeAdapter(context, grades)
        listView.adapter = arrayAdapter

        listView.setOnItemClickListener { _, _, position, _ ->
            val clickedHeadGrade = grades[position]
            openHeadGradeActivity(clickedHeadGrade)
        }
    }

    private fun observeViewModel() {
        viewModel.grades.observe(this) { list ->
            grades.clear()
            grades.addAll(list)
            arrayAdapter.notifyDataSetChanged()
        }
    }

    private fun setupListeners() {
        button.setOnClickListener {
            viewModel.addHeadGrade()
        }
    }

    private fun openHeadGradeActivity(headGrade: HeadGrade) {
        val intent = Intent(this, HeadGradeActivity::class.java)
        intent.putExtra("headGradeId", headGrade.id)
        startActivity(intent)
    }
}
