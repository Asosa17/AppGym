package net.azarquiel.appgym.view

import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.ui.AppBarConfiguration
import androidx.fragment.app.Fragment
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import net.azarquiel.appgym.R
import net.azarquiel.appgym.adapters.ComidaAdapter
import net.azarquiel.appgym.databinding.ActivityMainBinding
import net.azarquiel.appgym.model.Comida
import net.azarquiel.appgym.ui.LoginFragment
import net.azarquiel.appgym.ui.UserCompleteFragment
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var dialog: AlertDialog
    private var userlocal: FirebaseUser? = null
    private lateinit var fragment: Fragment
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var datosUserSH: SharedPreferences
    private lateinit var db: FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        datosUserSH= getSharedPreferences("datosUserSh", MODE_PRIVATE)
        val idioma = datosUserSH.getString("idioma","en").toString()
        val tema = datosUserSH.getString("tema","1").toString()
        detecidioma(idioma)
        detectatema(tema)

        db = Firebase.firestore
        auth = Firebase.auth
        val currentUser = auth.currentUser
        userlocal=currentUser
        buscaractu()
        setInitialFragment()

    }

    private fun buscaractu() {
        // Obtén el usuario autenticado
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userDocument = currentUser?.email?.let { db.collection("actu").document("actu") }
            userDocument!!.get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val actu = document.getBoolean("actu")!!
                        if (actu){
                            sacaactu()
                        }
                    }
                }
                .addOnFailureListener { e ->

                    Log.w("TAG", "Error al obtener datos del usuario: ", e)
                }
        } else {

        }
    }

    private fun sacaactu() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialoactu, null)
        val tvbuscaractu=dialogView.findViewById<TextView>(R.id.tvbuscaractu)
        tvbuscaractu.setOnClickListener {
            val url = "https://asosa17.github.io/AppGymWeb"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        }

        builder.setView(dialogView)
        dialog = builder.create()
        dialog.show()
        dialog.setCancelable(false)
    }

    private fun setInitialFragment() {
        val fragmentTransaction = supportFragmentManager.beginTransaction()

        if (userlocal==null){
            fragment = LoginFragment()
        }else{
            fragment = UserCompleteFragment()
        }
        fragmentTransaction.add(R.id.frame, fragment)
        fragmentTransaction.commit()
    }
    private fun detecidioma(idioma:String){
        val locale = Locale(idioma)
        val config = Configuration()
        Locale.setDefault(locale)
        config.setLocale(locale)

    }

    private fun detectatema(tema:String){
        val tema = tema.toInt()
        when(tema){
            1-> {
                setTheme(R.style.Base_Theme_TemaApp1)
            }
            2->{
                setTheme(R.style.Theme_TemaApp2)
            }
            3->{
                setTheme(R.style.Theme_TemaApp3)
            }

        }
    }
}