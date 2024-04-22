package net.azarquiel.appgym.view

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
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

class MainActivity : AppCompatActivity() {

    private var userlocal: FirebaseUser? = null
    private lateinit var fragment: Fragment
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

}