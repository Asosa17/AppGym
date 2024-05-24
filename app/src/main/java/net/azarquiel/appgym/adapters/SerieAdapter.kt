package net.azarquiel.appgym.adapters

import android.content.Context
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import net.azarquiel.appgym.R
import net.azarquiel.appgym.model.Serie

class SerieAdapter (val context: Context,
                    val layout: Int
) : RecyclerView.Adapter<SerieAdapter.ViewHolder>() {

    private var dataList: MutableList<Serie> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val viewlayout = layoutInflater.inflate(layout, parent, false)
        return ViewHolder(viewlayout, context)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataList[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    internal fun setSeries(series: MutableList<Serie>) {
        this.dataList = series
        notifyDataSetChanged()
    }

    class ViewHolder(viewlayout: View, val context: Context) : RecyclerView.ViewHolder(viewlayout) {
        fun bind(dataItem: Serie) {
            val edpesorowserie = itemView.findViewById<EditText>(R.id.edpesorowserie)
            edpesorowserie.text= Editable.Factory.getInstance().newEditable(dataItem.peso)
            val edrowserie = itemView.findViewById<EditText>(R.id.edrowserie)
            edrowserie.text= Editable.Factory.getInstance().newEditable(dataItem.reps)
        }
    }
}