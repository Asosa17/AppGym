package net.azarquiel.appgym.view

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import net.azarquiel.appgym.R
import net.azarquiel.appgym.databinding.ActivityPricipalBinding
import net.azarquiel.appgym.ui.AjustesFragment
import net.azarquiel.appgym.ui.ChatFragment
import net.azarquiel.appgym.ui.DietasFragment
import net.azarquiel.appgym.ui.HomeFragment
import net.azarquiel.appgym.ui.LoginFragment
import net.azarquiel.appgym.ui.RutinasFragment
import net.azarquiel.appgym.ui.UserCompleteFragment
import java.util.Locale

class PrincipalActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPricipalBinding
    private lateinit var fragment: Fragment
    private lateinit var datosUserSH: SharedPreferences
    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        datosUserSH= getSharedPreferences("datosUserSh", MODE_PRIVATE)
        val tema = datosUserSH.getString("tema","1").toString()
        detectatema(tema)

        binding = ActivityPricipalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        detectafondo(tema)

        setInitialFragment()
        checkLocationPermission()
        binding.navView.setOnItemSelectedListener { item ->
            if (item.itemId != binding.navView.selectedItemId) {
                when (item.itemId) {
                    R.id.navigation_home -> replaceFragment(HomeFragment())
                    R.id.navigation_rutinas -> replaceFragment(RutinasFragment())
                    R.id.navigation_chat -> replaceFragment(ChatFragment())
                    R.id.navigation_dietas -> replaceFragment(DietasFragment())
                    R.id.navigation_ajustes -> replaceFragment(AjustesFragment())
                }
            }
            true
        }


        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                binding.navView.selectedItemId=R.id.navigation_home
            }
        })

    }

    private fun detectafondo(tema: String) {
        val tema = tema.toInt()
        when(tema){
            1-> {
                binding.FrameLayout.setBackgroundResource(R.drawable.fondoajustes)
            }
            2->{
                binding.FrameLayout.setBackgroundResource(R.drawable.fondoajustesazul)
            }
            3->{
                binding.FrameLayout.setBackgroundResource(R.drawable.fondoajustesverde)
            }

        }
    }

    private fun setInitialFragment() {
        val fragmentTransaction = supportFragmentManager.beginTransaction()

        fragment=HomeFragment()
        fragmentTransaction.add(R.id.FrameLayout, fragment)
        fragmentTransaction.commit()
    }
    fun replaceFragment(fragment: Fragment) {
        val fragmentManager=supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.FrameLayout, fragment)
        fragmentTransaction.commit()
    }
    private fun detectatema(tema:String){
        val tema = tema.toInt()
        when(tema){
            1-> {
                setTheme(R.style.Base_Theme_TemaApp4)
            }
            2->{
                setTheme(R.style.Theme_TemaApp5)
            }
            3->{
                setTheme(R.style.Theme_TemaApp6)
            }

        }
    }
    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

}