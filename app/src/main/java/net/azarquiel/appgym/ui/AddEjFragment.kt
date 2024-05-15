package net.azarquiel.appgym.ui

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.SearchView
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import net.azarquiel.appgym.R
import net.azarquiel.appgym.adapters.AddEjAdapter
import net.azarquiel.appgym.adapters.EjercicioAdapter
import net.azarquiel.appgym.databinding.FragmentAddEjBinding
import net.azarquiel.appgym.model.Ejercicio
import net.azarquiel.appgym.model.Rutina


class AddEjFragment(rutina: Rutina) : DialogFragment(), SearchView.OnQueryTextListener {
    private lateinit var adapter: AddEjAdapter
    private lateinit var binding: FragmentAddEjBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var datosUserSH: SharedPreferences
    private var email: String? = null
    private lateinit var ejs: MutableList<Ejercicio>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddEjBinding.inflate(inflater, container, false)
        val root = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        datosUserSH = requireActivity().getSharedPreferences("datosUserSh", Context.MODE_PRIVATE)
        initRV()
    }
    private fun initRV() {
        email = datosUserSH.getString("email", "")
        ejs = mutableListOf<Ejercicio>()
        adapter = AddEjAdapter(requireContext(), R.layout.rowej,onClickListener)
        binding.rvaddejs.adapter=adapter
        binding.rvaddejs.layoutManager= LinearLayoutManager(requireContext())
        obtenerejs()
        val serachviewejs = binding.searchViewejs
        serachviewejs.setQueryHint("Search...")
        serachviewejs.setOnQueryTextListener(this)
        val text = serachviewejs.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
    }
    override fun onQueryTextChange(query: String): Boolean {
        val filteredList = ejs
        val filt=filteredList.filter { ejercicio -> ejercicio.NombreEj.contains(query, true) }
        adapter.setEjercicios(filt.toMutableList()) // Filtrar por nombre que contiene el texto de búsqueda, sin importar mayúsculas o minúsculas })
        adapter.notifyDataSetChanged()
        return true
    }
    override fun onQueryTextSubmit(text: String): Boolean {
        return false
    }
    private val onClickListener = object : AddEjAdapter.OnClickListenerRecycler {
        override fun OnClickAddEj(dataItem: Ejercicio){


        }
    }

    private fun obtenerejs() {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            val userEmail = user.email
            userEmail?.let { email ->
                val ejsdb = db.collection("/CATEGORIAS/TRICEPS/EJERCICIOS")
                ejsdb.get()
                    .addOnSuccessListener { documents ->
                        documentsToList(documents)
                        adapter.notifyDataSetChanged()
                    }
                    .addOnFailureListener { e ->
                    }
            }
        }
    }
    private fun documentsToList(documents: QuerySnapshot?){
        for (document in documents!!) {
            val id = document.id
            val imageUrl = document.getString("imageUrl")?:""
            val nombre = document.getString("nombre") ?: ""
            val descripcion = document.getString("descripcion") ?: ""
            val ejercicio = Ejercicio(id, nombre, imageUrl, descripcion)
            ejs.add(ejercicio)

        }
        adapter.setEjercicios(ejs)

    }


}