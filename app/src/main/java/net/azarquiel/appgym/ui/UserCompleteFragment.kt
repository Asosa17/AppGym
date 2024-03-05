package net.azarquiel.appgym.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import net.azarquiel.appgym.R
import net.azarquiel.appgym.view.PrincipalActivity


class UserCompleteFragment : Fragment() {
    private lateinit var root: View
    private lateinit var btncontinuar: Button
    private lateinit var tvusernamebvnd: TextView
    private var fragment: Fragment?=null
    private  var userlocal: FirebaseUser?=null
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
        auth = Firebase.auth
        db = Firebase.firestore
        getUser()
        tvusernamebvnd=root.findViewById<TextView>(R.id.tvusernamebvnd)
        tvusernamebvnd.text=userlocal!!.email
        btncontinuar=root.findViewById<Button>(R.id.btncontinuar)
        btncontinuar.setOnClickListener {
            Entrarapp()

        }
    }

    private fun getUser() {
        // Obtén el usuario autenticado
        val currentUser = auth.currentUser
        userlocal=currentUser
        if (userlocal != null) {
            val userDocument = userlocal?.email?.let { db.collection("users").document(it) }

            userDocument!!.get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        // El documento existe, obtén los datos del usuario
                        val username = document.getString("username")

                        tvusernamebvnd.text=username
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
        intent.putExtra("userlocal", userlocal)
        startActivity(intent)
        requireActivity().finish()
    }

}