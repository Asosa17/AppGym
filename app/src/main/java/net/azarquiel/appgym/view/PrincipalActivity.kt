package net.azarquiel.appgym.view

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
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

class PrincipalActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPricipalBinding
    private lateinit var fragment: Fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPricipalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setInitialFragment()

        binding.navView.setOnItemSelectedListener {
            when (it.itemId){
                R.id.navigation_home -> replaceFragment(HomeFragment())
                R.id.navigation_rutinas -> replaceFragment(RutinasFragment())
                R.id.navigation_chat -> replaceFragment(ChatFragment())
                R.id.navigation_dietas -> replaceFragment(DietasFragment())
                R.id.navigation_ajustes -> replaceFragment(AjustesFragment())

                else ->{

                }

            }
            true
        }

    }
    private fun setInitialFragment() {
        val fragmentTransaction = supportFragmentManager.beginTransaction()

        fragment=HomeFragment()
        fragmentTransaction.add(R.id.FrameLayout, fragment)
        fragmentTransaction.commit()
    }
    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager=supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.FrameLayout, fragment)
        fragmentTransaction.commit()
    }
}