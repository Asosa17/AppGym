package net.azarquiel.appgym.ui

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
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

class RegisterFragment : Fragment() {
    private lateinit var root: View
    private lateinit var btncancel: Button
    private lateinit var btnaceptarrg: Button
    private var fragment: Fragment?=null
    private var userlocal: FirebaseUser? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var etnombreuserrg: EditText
    private lateinit var etpassrg: EditText
    private lateinit var etemailrg: EditText
    private lateinit var db: FirebaseFirestore
    private lateinit var datosUserSH: SharedPreferences


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root= inflater.inflate(R.layout.fragment_register, container, false)
        return root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = Firebase.auth
        db = Firebase.firestore
        datosUserSH = requireActivity().getSharedPreferences("datosUserSh", Context.MODE_PRIVATE)
        etnombreuserrg= root.findViewById<EditText>(R.id.etnombreuserrg)
        etemailrg= root.findViewById<EditText>(R.id.etemailrg)
        etpassrg= root.findViewById<EditText>(R.id.etpassrg)
        btncancel=root.findViewById<Button>(R.id.btncancel)
        btncancel.setOnClickListener {
            replaceFragment(LoginFragment())
        }
        btnaceptarrg=root.findViewById<Button>(R.id.btnaceptarrg)
        btnaceptarrg.setOnClickListener {
            registro()

        }
    }

    private fun registro() {
        if (etemailrg.text.toString().isEmpty()||etpassrg.text.toString().isEmpty()||etnombreuserrg.text.toString().isEmpty()){
            msg("Campos Obligatorios")
            return
        }
        val email = etemailrg.text.toString()
        val password = etpassrg.text.toString()
        val postHast: MutableMap<String, Any> = HashMap()
        postHast["username"] = etnombreuserrg.text.toString()
        postHast["email"] = etemailrg.text.toString()
        postHast["pass"] = etpassrg.text.toString()
        // Verificar si el usuario ya existe
        db.collection("users").document(email).get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { signInTask ->
                            if (signInTask.isSuccessful) {
                                msg("Este usuario ya existe, Inicie sesion para continuar: ${signInTask.exception?.message}")
                            } else {
                                msg("Error al iniciar sesión: ${signInTask.exception?.message}")
                            }
                        }


                } else {
                    // Crear el usuario solo si no existe
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                userlocal = task.result?.user
                                val intent = Intent(requireContext(), PrincipalActivity::class.java)
                                intent.putExtra("userlocal", userlocal)
                                startActivity(intent)
                                requireActivity().finish()
                                // También puedes almacenar la información adicional en Firestore aquí
                                db.collection("users")
                                    .document(email)
                                    .set(postHast).addOnSuccessListener { documentReference ->
                                        Log.d("TAG","DocumentSnapshot added with ID: " + etemailrg.text.toString())
                                        etnombreuserrg.setText("")
                                        etemailrg.setText("")
                                        etpassrg.setText("") // clear editext// scrool arriba
                                        // hide teclado
                                    }
                                    .addOnFailureListener{ e ->
                                        Log.w("TAG","Error adding document", e)
                                    }
                            } else {
                                msg("Error ${task.exception?.message}")
                            }
                        }
                        meterDatosUser(etnombreuserrg.text.toString(),etemailrg.text.toString())
                }
            }
            .addOnFailureListener { e ->
                msg("Error al verificar usuario: ${e.message}")
            }
    }

    private fun replaceFragment(fragment: Fragment) {

        val fragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame, fragment)
        fragmentTransaction.commit()
    }
    private fun msg(msg: String) {
        Toast.makeText(requireActivity(),msg, Toast.LENGTH_LONG).show()
    }
    private fun meterDatosUser(username:String,email:String) {
        var editor = datosUserSH.edit()
        editor.putString("username", username)
        editor.putString("email", email)
        editor.commit()
    }
}