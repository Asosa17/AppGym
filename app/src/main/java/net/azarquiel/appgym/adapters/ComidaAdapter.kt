package net.azarquiel.appgym.adapters

import android.content.Context
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import net.azarquiel.appgym.R
import net.azarquiel.appgym.model.Comida

class ComidaAdapter (val context: Context,
                     val layout: Int
) : RecyclerView.Adapter<ComidaAdapter.ViewHolder>() {

    private var dataList: List<Comida> = emptyList()

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

    internal fun setComidas(comidas: List<Comida>) {
        this.dataList = comidas
    }


    class ViewHolder(viewlayout: View, val context: Context) : RecyclerView.ViewHolder(viewlayout) {
        fun bind(dataItem: Comida) {
            // itemview es el item de diseño
            // al que hay que poner los datos del objeto dataItem
            val edtcomida = itemView.findViewById(R.id.edtcomida) as EditText
            edtcomida.setText(dataItem.NombreComida)
            val edtcant = itemView.findViewById(R.id.edtcant) as EditText
            edtcant.setText(dataItem.Cantidad)
            val edtkcal = itemView.findViewById(R.id.edtkcal) as EditText
            edtkcal.setText(dataItem.Kcal100)
            val tvkcalTotales = itemView.findViewById(R.id.tvkcalTotales) as TextView
            tvkcalTotales.setText(dataItem.KcalTotales)

            itemView.tag = dataItem
        }
    }
}