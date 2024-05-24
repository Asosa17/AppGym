package net.azarquiel.appgym.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import net.azarquiel.appgym.R
import net.azarquiel.appgym.model.Comida
import net.azarquiel.appgym.model.Peso

class PesoAdapter (val context: Context,
                   val layout: Int

) : RecyclerView.Adapter<PesoAdapter.ViewHolder>(){

    private var dataList: MutableList<Peso> = mutableListOf()
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

    internal fun setPesos(pesos: MutableList<Peso>) {
        this.dataList = pesos
    }


    class ViewHolder(viewlayout: View, val context: Context) : RecyclerView.ViewHolder(viewlayout) {
        fun bind(dataItem: Peso) {
            // itemview es el item de diseño
            // al que hay que poner los datos del objeto dataItem
            val itempeso = itemView.findViewById<TextView>(R.id.itempeso)
            val itemfecha = itemView.findViewById<TextView>(R.id.itemfecha)

            itempeso.text = dataItem.peso
            itemfecha.text = dataItem.fecha


            itemView.tag = dataItem



        }
    }
}
