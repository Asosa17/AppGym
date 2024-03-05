package net.azarquiel.appgym.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import net.azarquiel.appgym.R
import net.azarquiel.appgym.databinding.ActivityPricipalBinding

class PrincipalActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPricipalBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPricipalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_rutinas, R.id.navigation_chat, R.id.navigation_dietas
            )
        )

        navView.setupWithNavController(navController)
    }


}