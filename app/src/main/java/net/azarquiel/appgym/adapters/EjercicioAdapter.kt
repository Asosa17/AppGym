package net.azarquiel.appgym.adapters

import android.app.Activity
import android.content.Context
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.squareup.picasso.Picasso
import net.azarquiel.appgym.R
import net.azarquiel.appgym.model.Ejercicio
import net.azarquiel.appgym.model.Serie

class EjercicioAdapter (val context: Context,
                        val layout: Int,
                        val listener: OnClickListenerRecycler
) : RecyclerView.Adapter<EjercicioAdapter.ViewHolder>(){

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
        private lateinit var seriesAdapter: SerieAdapter
        private val rvSeries: RecyclerView = itemView.findViewById(R.id.rvseriesej)


        fun bind(dataItem: Ejercicio, listener: OnClickListenerRecycler) {

            val tvnombreej = itemView.findViewById(R.id.tvnombreej) as TextView
            tvnombreej.setText(dataItem.NombreEj)
            val ivfotoej = itemView.findViewById(R.id.ivfotoej) as ImageView
            Glide.with(context).load(dataItem.Foto).into(ivfotoej)
            val btneliminarej=itemView.findViewById<Button>(R.id.btneliminarej) as Button
            val btnagregarserie=itemView.findViewById<Button>(R.id.btnagregarserie) as Button
            itemView.tag = dataItem
            itemView.setOnClickListener { listener.OnClickEj(dataItem) }
            btneliminarej.setOnClickListener { listener.OnClickEliminarEj(itemView) }
            btnagregarserie.setOnClickListener { listener.OnClickAddSerie(itemView) }
            // Configure the RecyclerView for the series
            seriesAdapter = SerieAdapter(context, R.layout.rowserie, object : SerieAdapter.OnClickListenerRecycler {
                override fun OnClickEliminarSerie(dataItem: Serie) {
                    // Implementa la lógica para eliminar una serie
                    listener.OnClickEliminarSerie(dataItem,itemView)
                }
            })
            rvSeries.adapter = seriesAdapter
            rvSeries.layoutManager = LinearLayoutManager(context)
            // Set the series data to the adapter
            seriesAdapter.setSeries(dataItem.Series.toMutableList())
        }
    }
    interface OnClickListenerRecycler {
        fun OnClickEj(dataItem: Ejercicio) {

        }
        fun OnClickEliminarEj(itemView: View) {

        }
        fun OnClickAddSerie(itemView: View) {


        }
        fun OnClickEliminarSerie(dataItem: Serie,itemView: View) {
            // Implementa la lógica para eliminar una serie
        }

    }
}