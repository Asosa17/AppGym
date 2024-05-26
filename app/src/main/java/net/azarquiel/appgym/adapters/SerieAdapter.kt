package net.azarquiel.appgym.adapters

import android.content.Context
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import net.azarquiel.appgym.R
import net.azarquiel.appgym.model.Ejercicio
import net.azarquiel.appgym.model.Serie

class SerieAdapter (val context: Context,
                    val layout: Int,
                    val listener: OnClickListenerRecycler
) : RecyclerView.Adapter<SerieAdapter.ViewHolder>() {


    private var dataList: MutableList<Serie> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val viewlayout = layoutInflater.inflate(layout, parent, false)
        return ViewHolder(viewlayout, context)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataList[position]
        holder.bind(item,listener)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }
    fun deleteItem(position: Int) {
        dataList.removeAt(position) // Elimina el elemento del array de comidas
        notifyItemRemoved(position) // Notifica al adaptador sobre el cambio
    }
    internal fun setSeries(series: MutableList<Serie>) {
        this.dataList = series
        notifyDataSetChanged()
    }

    class ViewHolder(viewlayout: View, val context: Context) : RecyclerView.ViewHolder(viewlayout) {
        fun bind(dataItem: Serie, listener: SerieAdapter.OnClickListenerRecycler) {
            val edpesorowserie = itemView.findViewById<TextView>(R.id.edpesorowserie)
            edpesorowserie.setText(dataItem.peso)
            val edrowserie = itemView.findViewById<TextView>(R.id.edrowserie)
            edrowserie.setText(dataItem.reps)
            val btnelimnarserie = itemView.findViewById<Button>(R.id.btnelimnarserie)
            btnelimnarserie.setOnClickListener { listener.OnClickEliminarSerie(dataItem) }
            itemView.tag = dataItem
        }
    }
    interface OnClickListenerRecycler {
        fun OnClickEliminarSerie(dataItem: Serie) {

        }



    }
}