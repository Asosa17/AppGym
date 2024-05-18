package net.azarquiel.appgym.ui

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import net.azarquiel.appgym.R
import net.azarquiel.appgym.adapters.AddEjAdapter
import net.azarquiel.appgym.databinding.FragmentAddEjBinding
import net.azarquiel.appgym.model.Ejercicio
import net.azarquiel.appgym.model.Rutina


class AddEjFragment(rutina: Rutina, ejerciciosFragment: EjerciciosFragment) : DialogFragment(), SearchView.OnQueryTextListener {
    private lateinit var adapter: AddEjAdapter
    private lateinit var binding: FragmentAddEjBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var datosUserSH: SharedPreferences
    private var email: String? = null
    private lateinit var ejs: MutableList<Ejercicio>
    private var rutina:Rutina=rutina
    private var ejerciciosFragment:EjerciciosFragment=ejerciciosFragment

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
        obtenerejs("ABDOMINALES")
        binding.tvaddejs.text = "ABDOMINALES"
        obtenercats()
        val serachviewejs = binding.searchViewejs
        serachviewejs.setQueryHint("Search...")
        serachviewejs.setOnQueryTextListener(this)
        val tema = datosUserSH.getString("tema","1").toString()
        detectafondo(tema)
        binding.ivcancelaraddej2.setOnClickListener {
            dismiss()
        }
    }
    private fun detectafondo(tema: String) {
        val tema = tema.toInt()
        when(tema){
            1-> {
                binding.cntladdej.setBackgroundResource(R.drawable.fondoajustes)
            }
            2->{
                binding.cntladdej.setBackgroundResource(R.drawable.fondoajustesazul)
            }
            3->{
                binding.cntladdej.setBackgroundResource(R.drawable.fondoajustesverde)
            }

        }
    }
    private fun obtenercats() {
        for (j in 0 until binding.lyhcategorias.childCount) {
            var textview = binding.lyhcategorias.getChildAt(j) as TextView
            textview.setOnClickListener { onclikcat( textview.text.toString()) }
        }
    }

    private fun onclikcat(catpulsada:String) {
        ejs.clear()
        obtenerejs(catpulsada)
        binding.tvaddejs.text = catpulsada
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
        override fun OnClickAddEj(itemView:View){
            val dataItem = itemView.tag as Ejercicio
            añadirej(dataItem)
            Toast.makeText(requireContext(),"Ejercicio añadido correctamente",Toast.LENGTH_SHORT).show()
        }
    }

    private fun añadirej(dataItem:Ejercicio) {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            val userEmail = user.email
            userEmail?.let { email ->
                val rutinasdb = db.collection("users").document(email)
                rutinasdb.get()
                    .addOnSuccessListener { document ->
                        val rutinas = document.data?.get("rutinas") as? MutableMap<String, Any>
                        if (rutinas != null) {
                            // Retrieve the list of exercises for the specific "rutina"
                            var ejercicios = rutinas[rutina.Nombre] as? MutableMap<String,Map<String, Any>>
                            if (ejercicios == null) {
                                // If the list is null, initialize it as an empty list
                                ejercicios = mutableMapOf()
                                rutinas[rutina.Nombre] = ejercicios
                                // Create a new map from the dataItem to match the Firestore format
                                val ejercicioMap = mapOf(
                                    "id" to dataItem.id,
                                    "nombre" to dataItem.NombreEj,
                                    "foto" to dataItem.Foto,
                                    "descripcion" to dataItem.Descripcion,
                                )
                                ejercicios[dataItem.id] = ejercicioMap
                                // Update the "rutinas" field in Firestore
                                rutinasdb.update("rutinas", rutinas)
                                    .addOnSuccessListener {
                                        // Successfully updated the Firestore document
                                        Log.d("Firestore", "Ejercicio actualizado correctamente")
                                    }
                                    .addOnFailureListener { e ->
                                        // Handle the error
                                        Log.e("Firestore", "Error actualizando el ejercicio", e)
                                    }
                            }else if (ejercicios != null) {
                                // Create a new map from the dataItem to match the Firestore format
                                val ejercicioMap = mapOf(
                                    "id" to dataItem.id,
                                    "nombre" to dataItem.NombreEj,
                                    "foto" to dataItem.Foto,
                                    "descripcion" to dataItem.Descripcion,
                                )
                                ejercicios[dataItem.id] = ejercicioMap
                                // Update the "rutinas" field in Firestore
                                rutinasdb.update("rutinas", rutinas)
                                    .addOnSuccessListener {
                                        // Successfully updated the Firestore document
                                        Log.d("Firestore", "Ejercicio actualizado correctamente")
                                    }
                                    .addOnFailureListener { e ->
                                        // Handle the error
                                        Log.e("Firestore", "Error actualizando el ejercicio", e)
                                    }
                            } else {
                                Log.e("Firestore", "No se encontró la lista de ejercicios")
                            }
                        } else {
                            Log.e("Firestore", "No se encontró el campo rutinas")
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("Firestore", "No se encontró el campo rutinas")
                    }
            }

        }
    }

    private fun obtenerejs(catpulsada: String) {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            val userEmail = user.email
            userEmail?.let { email ->
                val ejsdb = db.collection("/CATEGORIAS/${catpulsada}/EJERCICIOS")
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