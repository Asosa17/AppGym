package net.azarquiel.appgym.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import net.azarquiel.appgym.R
import net.azarquiel.appgym.model.Comentario

class ComentarioAdapter (val context: Context,
                         val layout: Int,
                         val listener: OnClickListenerRecycler
) : RecyclerView.Adapter<ComentarioAdapter.ViewHolder>(){

    private var dataList: MutableList<Comentario> = mutableListOf()
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

    internal fun setComentarios(coments: MutableList<Comentario>) {
        this.dataList = coments
    }


    class ViewHolder(viewlayout: View, val context: Context) : RecyclerView.ViewHolder(viewlayout) {
        fun bind(dataItem: Comentario, listener: OnClickListenerRecycler) {

            val tvnombreusercoment = itemView.findViewById(R.id.tvnombreusercoment) as TextView
            tvnombreusercoment.setText(dataItem.Usuario)
            val tvContenidoComent = itemView.findViewById(R.id.tvContenidoComent) as TextView
            tvContenidoComent.setText(dataItem.Contenido)


            itemView.tag = dataItem
//            btnlike.setOnClickListener { listener.onClickLike(itemView) }


        }
    }
    interface OnClickListenerRecycler {
        fun onClickLike(itemView: View) {

        }
    }
}