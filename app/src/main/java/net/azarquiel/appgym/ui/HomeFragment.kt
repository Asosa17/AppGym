package com.example.pruebamenu.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import net.azarquiel.appgym.R
import net.azarquiel.appgym.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private lateinit var root: View
    private var userlocal: FirebaseUser? = null
    private lateinit var auth: FirebaseAuth;
    private lateinit var db: FirebaseFirestore
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        root= inflater.inflate(R.layout.fragment_home, container, false)
        return root



    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = Firebase.auth
        db = Firebase.firestore

        var btnlogout=root.findViewById<Button>(R.id.btnlogout)
        btnlogout.setOnClickListener {
            logOut()
        }

    }

    private fun logOut() {
        auth.signOut()
        userlocal=null
    }

}