package de.kyrohpaneup.abirechner.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import de.kyrohpaneup.abirechner.data.database.Subject

class SubjectAdapter(
    context: Context,
    private val items: List<Subject>
) : ArrayAdapter<Subject>(context, 0, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)

        val subject = getItem(position)

        // Display only the second string
        val textView = view.findViewById<TextView>(android.R.id.text1)
        textView.text = subject?.name ?: ""

        return view
    }

    // Optional: Override if you want to access the full object
    override fun getItem(position: Int): Subject? {
        return items.getOrNull(position)
    }
}