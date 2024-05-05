package net.azarquiel.appgym.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import net.azarquiel.appgym.R
import net.azarquiel.appgym.model.Post

class PostAdapter (val context: Context,
                   val layout: Int,
                   val listener: OnClickListenerRecycler
) : RecyclerView.Adapter<PostAdapter.ViewHolder>(){

    private var dataList: MutableList<Post> = mutableListOf()
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

    internal fun setPosts(posts: MutableList<Post>) {
        this.dataList = posts
    }


    class ViewHolder(viewlayout: View, val context: Context) : RecyclerView.ViewHolder(viewlayout) {
        fun bind(dataItem: Post, listener: OnClickListenerRecycler) {
            // itemview es el item de diseño
            // al que hay que poner los datos del objeto dataItem
            val ivpost = itemView.findViewById(R.id.ivpost) as ImageView

            val tvpiepost = itemView.findViewById(R.id.tvpiepost) as TextView
            tvpiepost.setText(dataItem.PieComent)
            val tvcountlikes = itemView.findViewById(R.id.tvcountlikes) as TextView
            tvcountlikes.setText(dataItem.Likes)

            val btnlike = itemView.findViewById(R.id.btnlike) as ImageView
            val btncoment = itemView.findViewById(R.id.btncoment) as ImageView

            itemView.tag = dataItem
            btnlike.setOnClickListener { listener.onClickLike(itemView) }
            btncoment.setOnClickListener { listener.onClickComment(itemView) }


        }
    }
    interface OnClickListenerRecycler {
        fun onClickLike(itemView: View) {

        }
        fun onClickComment(itemView: View) {
        }

    }
}