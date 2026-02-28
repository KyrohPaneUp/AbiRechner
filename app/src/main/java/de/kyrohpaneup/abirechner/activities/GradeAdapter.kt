package de.kyrohpaneup.abirechner.activities

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import de.kyrohpaneup.abirechner.R
import de.kyrohpaneup.abirechner.data.grades.Grade

class GradeAdapter(context: Context, grades: List<Grade>) :
    ArrayAdapter<Grade>(context, 0, grades) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        val grade = getItem(position)

        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.parent_grade_item, parent, false)
        }

        val subjectTextView = view?.findViewById<TextView>(R.id.titleText)
        val notesTextView = view?.findViewById<TextView>(R.id.subtitleText)
        val weightTextView = view?.findViewById<TextView>(R.id.weightText)
        val gradeTextView = view?.findViewById<TextView>(R.id.gradeText)

        subjectTextView?.text =
            if (grade?.name.isNullOrBlank()) "N/A" else grade.name

        notesTextView?.text =
            if (grade?.notes.isNullOrBlank()) "N/A" else grade.notes

        weightTextView?.text =
            grade?.weight?.toString() ?: "N/A"

        gradeTextView?.text =
            grade?.grade?.toString() ?: "N/A"

        return view!!
    }
}