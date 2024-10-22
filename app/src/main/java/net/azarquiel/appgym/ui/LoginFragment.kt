package net.azarquiel.appgym.ui

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import net.azarquiel.appgym.R
import net.azarquiel.appgym.view.PrincipalActivity

class LoginFragment : Fragment() {
    private lateinit var root: View
    private lateinit var btnregistro: Button
    private lateinit var btnaceptar: Button
    private var userFB: FirebaseUser? = null
    private lateinit var auth: FirebaseAuth;
    private lateinit var etpass: EditText
    private lateinit var etemail: EditText
    private lateinit var db: FirebaseFirestore
    private lateinit var datosUserSH: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root= inflater.inflate(R.layout.fragment_login, container, false)
        return root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = Firebase.auth
        db = Firebase.firestore
        datosUserSH = requireActivity().getSharedPreferences("datosUserSh", Context.MODE_PRIVATE)
        etemail= root.findViewById<EditText>(R.id.etemail)
        etpass= root.findViewById<EditText>(R.id.etpass)
        btnregistro=root.findViewById<Button>(R.id.btnregs)
        btnregistro.setOnClickListener {
            replaceFragment(RegisterFragment())
        }
        btnaceptar=root.findViewById<Button>(R.id.btnIniSesion)
        btnaceptar.setOnClickListener {
            login()

        }
    }

    private fun login() {
        if (etemail.text.toString().isEmpty() || etpass.text.toString().isEmpty()) {
            Toast.makeText(requireActivity(), "Campos Obligatorios", Toast.LENGTH_LONG).show()
            return
        }
        val email = etemail.text.toString()
        val password = etpass.text.toString()
        db.collection("users").document(email).get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val usernameSH = documentSnapshot.getString("username")
                    val emailSH = documentSnapshot.getString("email")
                    val imgUrlSH = documentSnapshot.getString("imageUrl")
                    meterDatosUser(usernameSH!!, emailSH!!, imgUrlSH!!)
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { signInTask ->
                            if (signInTask.isSuccessful) {
                                userFB = auth.currentUser
                                val intent = Intent(requireContext(), PrincipalActivity::class.java)
                                intent.putExtra("userFB", userFB)
                                startActivity(intent)
                                requireActivity().finish()
                                etemail.setText("")
                                etpass.setText("")
                            } else {
                                Toast.makeText(requireActivity(), "Error al iniciar sesion", Toast.LENGTH_LONG).show()
                            }
                        }


                }else{
                    Toast.makeText(requireActivity(), "No existe ningun usuario con estas credenciales", Toast.LENGTH_LONG).show()

                }
            }
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame, fragment)
        fragmentTransaction.commit()
    }
    private fun meterDatosUser(username:String,email:String,imageUrl:String) {
        var editor = datosUserSH.edit()
        editor.putString("username", username)
        editor.putString("email", email)
        editor.putString("imageUrl", imageUrl)
        editor.commit()
    }

}