import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import net.azarquiel.appgym.databinding.FragmentAddPostBinding
import net.azarquiel.appgym.databinding.FragmentChatBinding
import net.azarquiel.appgym.model.Comentario
import net.azarquiel.appgym.model.Comida
import net.azarquiel.appgym.model.Post
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddPostFragment : Fragment() {

    private lateinit var binding: FragmentAddPostBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var formattedDate: String
    private lateinit var datosUserSH: SharedPreferences


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddPostBinding.inflate(inflater, container, false)
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
        val currentDate = Calendar.getInstance().time
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        formattedDate = sdf.format(currentDate)

        binding.btnpublicaraddpost.setOnClickListener {
            publicarPost()
        }
    }

//    private fun publicarPost() {
//        val foto = binding.ivapfotopost.drawable
//        val coment= binding.edappiecoment.text.toString()
//        val email= datosUserSH.getString("email","")
//
//        email?.let { userEmail ->
//            // Crear el objeto Post
//            val post = Post(foto.toString(), 0, listOf(), coment, userEmail, formattedDate)
//
//            // Referencia al documento de los posts
//                val postDocument = db.collection("posts").document("fechas")
//                postDocument.get()
//                    .addOnSuccessListener { document ->
//                        if (document != null) {
//                            val fechasmap = document.data?.get(formattedDate) as MutableMap<String, Any>?
//                            if (fechasmap != null) {
//                                // Obtener el mapa de comidas para la fecha actual o crear uno nuevo si no existe
//                                val postsfecha = fechasmap.getOrPut(formattedDate) { mutableMapOf<String, Any>() } as MutableMap<String, Any>
//                                // Generar un ID único para la nueva comida
//                                val postid = "post"+(postsfecha.count()+1).toString()
//                                // Guardar los datos de la nueva comida en el mapa de comidas
//                                postsfecha[postid] = mapOf(
//                                    "Foto" to post.Foto,
//                                    "Comentarios" to post.Comentarios,
//                                    "Likes" to post.Likes,
//                                    "PieComent" to post.PieComent,
//                                    "Fecha" to post.Fecha,
//                                    "Usuraio" to post.Usuraio
//                                )
//                                // Actualizar el mapa de comidas en Firestore
//                                postDocument.update("fechas", fechasmap)
//                                    .addOnSuccessListener {
//                                        // La comida se guardó exitosamente
//                                        Toast.makeText(requireContext(), "Comida guardada correctamente", Toast.LENGTH_SHORT).show()
//                                    }
//                                    .addOnFailureListener { exception ->
//                                        // Manejar errores al guardar la comida
//                                        Log.e("DietasFragment", "Error al guardar la comida", exception)
//                                    }
//                            }
//                        }
//                    }
//                    .addOnFailureListener { exception ->
//                        // Manejar errores al obtener el mapa de comidas
//                        Log.e("DietasFragment", "Error al obtener comidas del usuario", exception)
//                    }
//            }
//        }

    private fun publicarPost() {
        val foto = binding.ivapfotopost.drawable
        val coment = binding.edappiecoment.text.toString()
        val email = datosUserSH.getString("email", "")
        val currentDate = Calendar.getInstance().time
        val sdf = SimpleDateFormat("ddMMyyyy", Locale.getDefault())
        val formattedDate2 = sdf.format(currentDate)
        val post = Post(foto.toString(), 0, listOf(), coment, email.toString(), formattedDate," ")
        email?.let { userEmail ->
            // Referencia al documento del usuario
            val postDocument = db.collection("posts").document("fechas")
            postDocument.get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val postsMapa = document.data?.getOrPut(formattedDate2) { mutableMapOf<String, Any>() } as MutableMap<String, Any>
                            // Generar un ID único para la nueva comida
                            val comidaId = "post"+(postsMapa!!.count()+1).toString()
                            // Guardar los datos de la nueva comida en el mapa de comidas
                            postsMapa[comidaId] = mapOf(
                                "Foto" to post.Foto,
                                "Comentarios" to post.Comentarios,
                                "Likes" to post.Likes,
                                "PieComent" to post.PieComent,
                                "Fecha" to post.Fecha,
                                "Usuraio" to post.Usuraio,
                                "id" to comidaId
                            )
                            // Actualizar el mapa de comidas en Firestore
                            postDocument.update("${formattedDate2}", postsMapa)
                                .addOnSuccessListener {
                                    // La comida se guardó exitosamente
                                    Toast.makeText(requireContext(), "Comida guardada correctamente", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener { exception ->
                                    // Manejar errores al guardar la comida
                                    Log.e("DietasFragment", "Error al guardar la comida", exception)
                                }

                    }

                }
                .addOnFailureListener { exception ->
                    // Manejar errores al obtener el mapa de comidas
                    Log.e("DietasFragment", "Error al obtener comidas del usuario", exception)
                }
        
        }
    }
}