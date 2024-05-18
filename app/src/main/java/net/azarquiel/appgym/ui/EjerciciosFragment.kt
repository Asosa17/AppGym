package net.azarquiel.appgym.ui

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import net.azarquiel.appgym.R
import net.azarquiel.appgym.adapters.EjercicioAdapter
import net.azarquiel.appgym.databinding.FragmentEjerciciosBinding
import net.azarquiel.appgym.model.Ejercicio
import net.azarquiel.appgym.model.Rutina


class EjerciciosFragment(rutina: Rutina) :  DialogFragment(){

    private lateinit var adapter: EjercicioAdapter
    private lateinit var binding: FragmentEjerciciosBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var datosUserSH: SharedPreferences
    private var email: String? = null
    private var rutina: Rutina = rutina
    private lateinit var Ejs: MutableList<Ejercicio>
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEjerciciosBinding.inflate(inflater, container, false)
        val root = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        datosUserSH = requireActivity().getSharedPreferences("datosUserSh", Context.MODE_PRIVATE)
        initRV()
        val tema = datosUserSH.getString("tema","1").toString()
        detectafondo(tema)
    }
    private fun initRV() {
        email = datosUserSH.getString("email", "")
        adapter = EjercicioAdapter(requireContext(), R.layout.rowej,onClickListener)
        Ejs = mutableListOf<Ejercicio>()
        binding.rvejs.adapter=adapter
        binding.rvejs.layoutManager= LinearLayoutManager(requireContext())
        obtenerEjs()
        binding.fabaaddej.setOnClickListener {
            añadirej()
        }
        binding.ivcancelaraddej.setOnClickListener {
            dismiss()
        }
    }
    private fun detectafondo(tema: String) {
        val tema = tema.toInt()
        when(tema){
            1-> {
                binding.cntlfej.setBackgroundResource(R.drawable.fondoajustes)
            }
            2->{
                binding.cntlfej.setBackgroundResource(R.drawable.fondoajustesazul)
            }
            3->{
                binding.cntlfej.setBackgroundResource(R.drawable.fondoajustesverde)
            }

        }
    }
    private val onClickListener = object : EjercicioAdapter.OnClickListenerRecycler {
        override fun OnClickEliminarEj(itemView: View) {
            val ejercicio = itemView.tag as Ejercicio // Obtener el objeto Comida asociado al itemView
            val position = Ejs.indexOf(ejercicio) // Encontrar la posición del elemento en el array de comidas
            adapter.deleteItem(position) // Eliminar el elemento del RecyclerView y del array de comidas
            adapter.notifyDataSetChanged()
            emilinarej(ejercicio)
        }
    }

    private fun emilinarej(ejercicio: Ejercicio) {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            val userEmail = user.email
            userEmail?.let { email ->
                val rutinasdb = db.collection("users").document(email)
                rutinasdb.get()
                    .addOnSuccessListener { document ->
                        val rutinas = document.data?.get("rutinas") as? MutableMap<String, MutableMap<String, Map<String, Any>>>
                        if (rutinas != null) {
                            val ejercicios = rutinas[rutina.Nombre]
                            if (ejercicios != null && ejercicios.containsKey(ejercicio.id)) {
                                // Remove the exercise from the map
                                ejercicios.remove(ejercicio.id)

                                // Update the "rutinas" field in Firestore
                                rutinasdb.update("rutinas", rutinas)
                                    .addOnSuccessListener {
                                        // Ejercicio eliminado con éxito
                                        Log.d("Firestore", "Ejercicio eliminado correctamente")
                                    }
                                    .addOnFailureListener { e ->
                                        // Manejar errores al actualizar la lista de ejercicios en la base de datos
                                        Log.e("Firestore", "Error eliminando el ejercicio", e)
                                    }
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        // Manejar errores al obtener la lista de rutinas del usuario desde la base de datos
                    }
            }
        }
    }

    private fun obtenerEjs() {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            val userEmail = user.email
            userEmail?.let { email ->
                val rutinasdb = db.collection("users").document(email)
                rutinasdb.addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.w("TAG", "Listen failed.", e)
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.exists()) {
                        val rutinas = snapshot.data?.get("rutinas") as? MutableMap<String, Any>
                        rutinas?.let {
                            val ejercicios = it[rutina.Nombre] as? MutableMap<String, Map<String, Any>>
                            ejercicios?.let {
                                if (!ejercicios.isNullOrEmpty()) {
                                    Ejs.clear()
                                    documentToListEjs(ejercicios)
                                    adapter.setEjercicios(Ejs)
                                    adapter.notifyDataSetChanged()
                                }
                            }
                        }
                    } else {
                        Log.d("TAG", "Current data: null")
                    }
                }
            }
        }
    }
    private fun añadirej() {
        val AddEjFragment = AddEjFragment(rutina,this)
        AddEjFragment.setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogTheme)
        AddEjFragment.show(childFragmentManager, "AddEjFragment.TAG")
    }
    private fun documentToListEjs(ejs: MutableMap<String,Map<String,Any>>) {
        ejs.forEach { (id, ejercicioMap) ->
            val id = ejercicioMap["id"] as? String ?: ""
            val nombre = ejercicioMap["nombre"] as? String ?: ""
            val foto = ejercicioMap["foto"] as? String ?: ""
            val descripcion = ejercicioMap["descripcion"] as? String ?: ""
            val ejercicio = Ejercicio(id,nombre, foto, descripcion)
            Ejs.add(ejercicio)
        }
    }


}