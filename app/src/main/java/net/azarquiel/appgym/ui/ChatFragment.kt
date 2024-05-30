package net.azarquiel.appgym.ui


import AddPostFragment
import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import net.azarquiel.appgym.R
import net.azarquiel.appgym.adapters.ComentarioAdapter
import net.azarquiel.appgym.adapters.PostAdapter
import net.azarquiel.appgym.databinding.FragmentChatBinding
import net.azarquiel.appgym.model.Comentario
import net.azarquiel.appgym.model.Post
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ChatFragment : Fragment(), PostAdapter.OnClickListenerRecycler  {

    private lateinit var edcomentdialog: EditText
    private lateinit var adaptercoments: ComentarioAdapter
    private lateinit var rvcomentarios: MutableList<Comentario>
    public lateinit var posts: MutableList<Post>
    private lateinit var datosUserSH: SharedPreferences
    private lateinit var adapter:  PostAdapter
    private lateinit var binding: FragmentChatBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var formattedDate: String
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChatBinding.inflate(inflater, container, false)
        val root = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        datosUserSH = requireActivity().getSharedPreferences("datosUserSh", Context.MODE_PRIVATE)
        initRV()

    }

    private fun initRV() {
        adapter = PostAdapter(requireContext(), R.layout.rowpost,onClickListener)
        posts = mutableListOf<Post>()
        binding.rvchat.adapter=adapter
        binding.rvchat.layoutManager= LinearLayoutManager(requireContext())

        adaptercoments = ComentarioAdapter(requireContext(), R.layout.rowcoment,onClickListenerComents)
        rvcomentarios = mutableListOf<Comentario>()

        val currentDate = Calendar.getInstance().time
        val sdf = SimpleDateFormat("ddMMyyyy", Locale.getDefault())
        formattedDate = sdf.format(currentDate)
        obtenerPost(formattedDate)
        binding.fabaAdirpost.setOnClickListener {
            val addPostFragment = AddPostFragment(this)
            addPostFragment.setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogTheme)
            addPostFragment.show(childFragmentManager, "AddPostFragment.TAG")
        }
        binding.fabactualizarrv.setOnClickListener {

            obtenerPost(formattedDate)
            binding.rvchat.smoothScrollToPosition(0)
        }
    }
    private val onClickListenerComents = object : ComentarioAdapter.OnClickListenerRecycler {

    }
    private val onClickListener = object : PostAdapter.OnClickListenerRecycler {
        override fun onClickComment(itemView: View,tvcountcoment:TextView) {
            val post = itemView.tag as Post // Obtener el objeto post asociado al itemView
            var tvcountcoment = tvcountcoment
            // Crear el Bottom Sheet
            val bottomSheetDialog = BottomSheetDialog(requireContext())
            val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_comments, null)
            edcomentdialog= bottomSheetView.findViewById<EditText>(R.id.edcomentdialog)
            val btnpublishcommentdialog= bottomSheetView.findViewById<ImageView>(R.id.btnpublishcommentdialog)
            val rvcomentariosdialog= bottomSheetView.findViewById<RecyclerView>(R.id.rvcomentariosdialog)

            rvcomentariosdialog.adapter=adaptercoments
            rvcomentariosdialog.layoutManager=LinearLayoutManager(requireContext())

            obtenerComments(post.id)
            adaptercoments.setComentarios(rvcomentarios)
            adaptercoments.notifyDataSetChanged()

            btnpublishcommentdialog.setOnClickListener {
                publicarComments(post)
                rvcomentariosdialog.smoothScrollToPosition(0)

            }
            // Mostrar el Bottom Sheet
            bottomSheetDialog.setContentView(bottomSheetView)
            bottomSheetDialog.show()
        }
        override fun onClickLike(itemView: View) {
            val post = itemView.tag as Post // Obtener el objeto Post asociado al itemView
            // Obtener el usuario actualmente autenticado
            val currentUser = auth.currentUser
            currentUser?.let { user ->
                val userEmail = user.email
                userEmail?.let { email ->
                    val postdb=db.collection("posts").document(formattedDate).collection("posts").document(post.id)
                    val postTotalesdb=db.collection("postsTotales").document(post.id)
                    postTotalesdb.get()
                        .addOnSuccessListener { document ->
                            val likes = document["Likes"] as?  List<String> ?: mutableListOf()
                            likes?.let {
                                if (it.contains(email)) {
                                    val updatedLikes = it.toMutableList()
                                    updatedLikes.remove(email)
                                    postdb.update("Likes",updatedLikes)
                                    postTotalesdb.update("Likes", updatedLikes)
                                        .addOnSuccessListener {
                                            post.Likes.remove(email)
                                            adapter.notifyDataSetChanged()
                                        }
                                        .addOnFailureListener { e ->
                                        }
                                }else{
                                    val updatedLikes = it.toMutableList()
                                    updatedLikes.add(email)
                                    postdb.update("Likes",updatedLikes)
                                    postTotalesdb.update("Likes", updatedLikes)
                                        .addOnSuccessListener {
                                            post.Likes.add(email)
                                            adapter.notifyDataSetChanged()
                                        }
                                        .addOnFailureListener { e ->
                                        }
                                }
                            }
                        }
                        .addOnFailureListener { e ->
                            // Manejar errores
                        }
                }
            }
        }
    }

    private fun obtenerComments(postid: String) {
        val comentsdb = db.collection("postsTotales").document(postid)
        comentsdb.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val coments = document["Comentarios"] as? MutableList<Map<String,Any>>
                    if (coments != null) {
                        // Limpiar la lista de comentarios antes de agregar los nuevos
                        rvcomentarios.clear()
                        // Convertir los datos de los comentarios a objetos Comentario y agregarlos a la lista
                        documentToListComments(coments)
                        // Notificar al adaptador de comentarios que los datos han cambiado
                        adaptercoments.notifyDataSetChanged()

                    }
                } else {

                }
            }
    }

    private fun publicarComments(post: Post) {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            val userEmail = user.email
            userEmail?.let { email ->
                val posts=db.collection("posts").document(formattedDate).collection("posts")
                val comentsdb = db.collection("postsTotales").document(post.id)
                comentsdb.get()
                    .addOnSuccessListener { document ->
                        val comentarios = document["Comentarios"] as? MutableList<Map<String, Any>>
                        comentarios?.let {
                            val newCommentMap = mutableMapOf<String, Any>()
                            newCommentMap["Contenido"] = edcomentdialog.text.toString()
                            newCommentMap["Usuario"] = datosUserSH.getString("username",null).toString()
                            newCommentMap["Likes"] = 0

                            val newComment = Comentario(edcomentdialog.text.toString(), datosUserSH.getString("username",null).toString(), 0)
                            // Agregar el nuevo comentario al array de comentarios en la base de datos
                            comentarios.add(newCommentMap)
                            // Actualizar el documento en la base de datos con la lista actualizada de comentarios
                            posts.document(post.id).update("Comentarios",comentarios)
                            comentsdb.update("Comentarios", comentarios)
                                .addOnSuccessListener {
                                    // Agregar el nuevo comentario al array de comentarios del objeto Post
                                    rvcomentarios.add(newComment)
                                    post.Comentarios.add(newComment)
                                    // Notificar al adaptador del RecyclerView de comentarios para actualizar la vista
                                    rvcomentarios.reverse()
                                    adaptercoments.setComentarios(rvcomentarios)
                                    adapter.notifyDataSetChanged()
                                    adaptercoments.notifyDataSetChanged()

                                }
                                .addOnFailureListener { e ->
                                    // Manejar errores al actualizar el documento en la base de datos
                                }
                        }
                    }
                    .addOnFailureListener { e ->
                        // Manejar errores al obtener la lista de comentarios del documento en la base de datos
                    }
            }
        }
    }

    private fun obtenerPost(formatedDate: String) {
        // Obtener el usuario actualmente autenticado
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            val userEmail = user.email
            userEmail?.let { email ->
                // Referencia al documento del usuario
                db.collection("postsTotales").get()
                    .addOnSuccessListener {
                        it?.let {
                            posts.clear()
                            documentToList(it.documents)
                            posts.reverse()
                            adapter.setPosts(posts)
                            adapter.notifyDataSetChanged()
                        }
                    }
                    .addOnFailureListener{exception ->
                        Toast.makeText(requireContext(), "No hay posts para la fecha $formatedDate",Toast.LENGTH_SHORT).show()
                    }

            }
        }
    }
    private fun documentToList(documents: MutableList<DocumentSnapshot>) {

        documents.forEach { document ->
            val postData = document.data
            val Foto = postData?.get("Foto") as String
            val Likes = postData?.get("Likes") as MutableList<String>
            val Comentarios = postData?.get("Comentarios") as MutableList<Comentario>
            val PieComent = postData?.get("PieComent") as String
            val Usuario = postData?.get("Usuario") as String
            val Fecha = postData?.get("Fecha") as String
            val id = postData?.get("id") as String
//            val imageUrl = runBlocking {
//                getImageUrl(id)
//            }
            posts.add(Post(Foto, Likes, Comentarios, PieComent, Usuario, Fecha, id))
        }
    }
    private fun documentToListComments(comentarios: MutableList<Map<String, Any>>) {
        comentarios.forEach { comentarioMap ->
            val contenido = comentarioMap["Contenido"] as String
            val usuario = comentarioMap["Usuario"] as String
            val likes = comentarioMap["Likes"] as Long
            val comentario = Comentario(contenido, usuario, likes) // Convertir likes de Long a Int
            rvcomentarios.add(comentario)
        }
    }

    fun refresh(posts:MutableList<Post>,formatedDate: String) {
        obtenerPost(formatedDate)
        binding.rvchat.smoothScrollToPosition(0)
    }


}
