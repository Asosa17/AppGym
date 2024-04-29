package net.azarquiel.appgym.ui

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import net.azarquiel.appgym.R
import net.azarquiel.appgym.model.User
import net.azarquiel.appgym.view.PrincipalActivity
import java.util.Locale


class UserCompleteFragment : Fragment() {
    private lateinit var datosUserSH: SharedPreferences
    private lateinit var imageUrl: String
    private lateinit var root: View
    private lateinit var btncontinuar: Button
    private lateinit var tvusernamebvnd: TextView
    private var fragment: Fragment?=null
    private  var userFB: FirebaseUser?=null
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root= inflater.inflate(R.layout.fragment_user_complete, container, false)
        return root;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        datosUserSH = requireActivity().getSharedPreferences("datosUserSh", MODE_PRIVATE)
        val tema = datosUserSH.getString("tema","1").toString()
        detectatema(tema)
        auth = Firebase.auth
        db = Firebase.firestore
        tvusernamebvnd=root.findViewById<TextView>(R.id.tvusernamebvnd)

        val username = datosUserSH.getString("username", null)
        if (!username.isNullOrEmpty()) {
            tvusernamebvnd.setText(username)
        }
        getUser()
        btncontinuar=root.findViewById<Button>(R.id.btncontinuar)
        btncontinuar.setOnClickListener {
            Entrarapp()

        }
    }

    private fun getUser() {
        // Obtén el usuario autenticado
        val currentUser = auth.currentUser
        userFB=currentUser
        if (userFB != null) {
            val userDocument = userFB?.email?.let { db.collection("users").document(it) }
            userDocument!!.get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        // El documento existe, obtén los datos del usuario
                        imageUrl = document.getString("imageUrl")!!
                        meterDatosUser(imageUrl)
                    } else {
                        // El documento no existe, maneja esta situación según tus requerimientos
                    }
                }
                .addOnFailureListener { e ->
                    // Maneja cualquier error que pueda ocurrir al realizar la consulta
                    Log.w("TAG", "Error al obtener datos del usuario: ", e)
                }
        } else {
            // El usuario no está autenticado, maneja esta situación según tus requerimientos
        }
    }

    private fun Entrarapp() {
        val intent = Intent(requireContext(), PrincipalActivity::class.java)
        intent.putExtra("userlocal", userFB)
        startActivity(intent)
        requireActivity().finish()
    }
    //SharePreferences
    private fun meterDatosUser(imageUrl:String) {
        var editor = datosUserSH.edit()
        editor.putString("imageUrl", imageUrl)
        editor.commit()
    }
    private fun detectatema(tema:String){
        val tema = tema.toInt()
        when(tema){
            1-> {
                val ivfondoiniuc=root.findViewById<ImageView>(R.id.ivfondoiniuc)
                ivfondoiniuc.setImageResource(R.drawable.fondorojohd)
            }
            2->{
                val ivfondoiniuc=root.findViewById<ImageView>(R.id.ivfondoiniuc)
                ivfondoiniuc.setImageResource(R.drawable.fondoiniazul)
            }
            3->{
                val ivfondoiniuc=root.findViewById<ImageView>(R.id.ivfondoiniuc)
                ivfondoiniuc.setImageResource(R.drawable.fondoiniverdeuc)
            }

        }
    }
}