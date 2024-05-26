package net.azarquiel.appgym.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.bumptech.glide.Glide;
import net.azarquiel.appgym.R
import net.azarquiel.appgym.model.Ejercicio

class AddEjAdapter (val context: Context,
                    val layout: Int,
                    val listener: OnClickListenerRecycler
) : RecyclerView.Adapter<AddEjAdapter.ViewHolder>(){

    private var dataList: MutableList<Ejercicio> = mutableListOf()
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

    internal fun setEjercicios(ejs: MutableList<Ejercicio>) {
        this.dataList = ejs
    }


    class ViewHolder(viewlayout: View, val context: Context) : RecyclerView.ViewHolder(viewlayout) {
        fun bind(dataItem: Ejercicio, listener: OnClickListenerRecycler) {
            // itemview es el item de diseño
            // al que hay que poner los datos del objeto dataItem
            val tvnombreej = itemView.findViewById(R.id.tvnombreej) as TextView
            tvnombreej.setText(dataItem.NombreEj)
            val ivfotoej = itemView.findViewById(R.id.ivfotoej) as ImageView

            Glide.with(context).load(dataItem.Foto).into(ivfotoej)
            val btneliminarej=itemView.findViewById<Button>(R.id.btneliminarej) as Button
            btneliminarej.visibility= View.GONE
            val btnagregarserie=itemView.findViewById<Button>(R.id.btnagregarserie) as Button
            btnagregarserie.visibility= View.GONE
            val tvrepsserire=itemView.findViewById<TextView>(R.id.tvrepsserire) as TextView
            tvrepsserire.visibility= View.GONE
            val tvkgserie=itemView.findViewById<TextView>(R.id.tvkgserie) as TextView
            tvkgserie.visibility= View.GONE


            itemView.tag = dataItem
            itemView.setOnClickListener { listener.OnClickAddEj(itemView) }

        }
    }
    interface OnClickListenerRecycler {
        fun OnClickAddEj(itemView: View) {

        }
    }
}