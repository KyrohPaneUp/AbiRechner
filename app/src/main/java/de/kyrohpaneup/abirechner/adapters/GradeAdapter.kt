package de.kyrohpaneup.abirechner.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import de.kyrohpaneup.abirechner.R
import de.kyrohpaneup.abirechner.data.database.Grade

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
            if (grade?.name.isNullOrBlank()) "N/A" else grade?.name

        notesTextView?.text =
            if (grade?.notes.isNullOrBlank()) "N/A" else grade?.notes

        var weight = "N/A"
        if (grade?.weight != null) weight = grade.weight.toString() + "%"
        weightTextView?.text = weight

        var points = "N/A"
        if (grade?.grade != null) points = grade.grade.toString() + "P."
        gradeTextView?.text = points

        return view!!
    }
}