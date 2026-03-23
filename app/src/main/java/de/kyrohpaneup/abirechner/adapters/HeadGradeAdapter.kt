package de.kyrohpaneup.abirechner.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import de.kyrohpaneup.abirechner.R
import de.kyrohpaneup.abirechner.data.GradeManager
import de.kyrohpaneup.abirechner.data.database.HeadGrade

class HeadGradeAdapter(
    context: Context,
    headGrades: List<HeadGrade>,
) : ArrayAdapter<HeadGrade>(context, 0, headGrades) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        val headGrade = getItem(position)

        val gradeManager = GradeManager()

        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.head_grade_item, parent, false)
        }

        val yearTextView = view?.findViewById<TextView>(R.id.titleText)
        val teacherTextView = view?.findViewById<TextView>(R.id.subtitleText)
        val gradeTextView = view?.findViewById<TextView>(R.id.gradeText)

        yearTextView?.text = if (headGrade?.year == null) "N/A" else gradeManager.getYearFromId(headGrade.year)

        teacherTextView?.text =
            if (headGrade?.teacher.isNullOrBlank()) "N/A" else headGrade?.teacher

        gradeTextView?.text =
            if (headGrade?.grade == null) "N/A" else headGrade.grade.toString()
        return view!!
    }
}