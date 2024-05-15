package net.azarquiel.appgym.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import net.azarquiel.appgym.R
import net.azarquiel.appgym.model.Rutina

class RutinaAdapter (val context: Context,
                     val layout: Int,
                     val listener: OnClickListenerRecycler
) : RecyclerView.Adapter<RutinaAdapter.ViewHolder>(){

    private var dataList: MutableList<Rutina> = mutableListOf()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val viewlayout = layoutInflater.inflate(layout, parent, false)
        return ViewHolder(viewlayout, context)

    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataList[position]
        holder.bind(item,listener)

    }

    fun deleteItem(position: Int) {
        dataList.removeAt(position) // Elimina el elemento del array de comidas
        notifyItemRemoved(position) // Notifica al adaptador sobre el cambio
    }
    override fun getItemCount(): Int {
        return dataList.size
    }

    internal fun setRutinas(rutinas: MutableList<Rutina>) {
        this.dataList = rutinas
    }


    class ViewHolder(viewlayout: View, val context: Context) : RecyclerView.ViewHolder(viewlayout) {
        fun bind(dataItem: Rutina, listener: OnClickListenerRecycler) {
            // itemview es el item de diseño
            // al que hay que poner los datos del objeto dataItem
            val tvnombrerutina = itemView.findViewById(R.id.tvnombrerutina) as TextView
            tvnombrerutina.setText(dataItem.Nombre)
            val btneliminarrutina=itemView.findViewById<Button>(R.id.btneliminarrutina) as Button

            itemView.tag = dataItem
            itemView.setOnClickListener { listener.OnClickRutina(itemView) }
            btneliminarrutina.setOnClickListener { listener.OnClickEliminarRutina(itemView) }


        }
    }
    interface OnClickListenerRecycler {
        fun OnClickRutina(itemView: View) {

        }
        fun OnClickEliminarRutina(itemView: View) {

        }


    }
}