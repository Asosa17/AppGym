package net.azarquiel.appgym.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import net.azarquiel.appgym.R
import net.azarquiel.appgym.model.Comida

class ComidaAdapter (val context: Context,
                     val layout: Int,
                     val listener: OnClickListenerRecycler
                ) : RecyclerView.Adapter<ComidaAdapter.ViewHolder>(){

    private var dataList: MutableList<Comida> = mutableListOf()
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

    internal fun setComidas(comidas: MutableList<Comida>) {
        this.dataList = comidas
    }


    class ViewHolder(viewlayout: View, val context: Context) : RecyclerView.ViewHolder(viewlayout) {
        fun bind(dataItem: Comida, listener: OnClickListenerRecycler) {
            // itemview es el item de diseño
            // al que hay que poner los datos del objeto dataItem
            val edtcomida = itemView.findViewById(R.id.edtcomida) as TextView
            edtcomida.setText(dataItem.NombreComida)
            val edtcant = itemView.findViewById(R.id.edtcant) as TextView
            edtcant.setText(dataItem.Cantidad)
            val edtkcal = itemView.findViewById(R.id.edtkcal) as TextView
            edtkcal.setText(dataItem.Kcal100)
            val tvkcalTotales = itemView.findViewById(R.id.tvkcalTotales) as TextView
            tvkcalTotales.setText(dataItem.KcalTotales)
            val btneliminarcomidadf = itemView.findViewById(R.id.btneliminarcomidadf) as Button
            itemView.tag = dataItem
            btneliminarcomidadf.setOnClickListener { listener.onClickEliminar(itemView) }
            itemView.setOnClickListener { listener.OnClickComida(itemView,btneliminarcomidadf) }


        }
    }
    interface OnClickListenerRecycler {
        fun OnClickComida(itemView: View, btneliminarcomidadf: Button) {

        }
        fun onClickEliminar(itemView: View) {
        }

    }
}


