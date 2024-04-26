package net.azarquiel.appgym.view

import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.navigation.ui.AppBarConfiguration
import androidx.fragment.app.Fragment
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import net.azarquiel.appgym.R
import net.azarquiel.appgym.databinding.ActivityMainBinding
import net.azarquiel.appgym.ui.LoginFragment
import net.azarquiel.appgym.ui.UserCompleteFragment
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private var userlocal: FirebaseUser? = null
    private lateinit var fragment: Fragment
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var datosUserSH: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        datosUserSH= getSharedPreferences("datosUserSh", MODE_PRIVATE)
        val idioma = datosUserSH.getString("idioma","en").toString()
        detecidioma(idioma)

        auth = Firebase.auth
        val currentUser = auth.currentUser
        userlocal=currentUser
        setInitialFragment()
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
}