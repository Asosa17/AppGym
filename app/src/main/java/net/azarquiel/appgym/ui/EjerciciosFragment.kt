package net.azarquiel.appgym.ui

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import net.azarquiel.appgym.R
import net.azarquiel.appgym.adapters.EjercicioAdapter
import net.azarquiel.appgym.databinding.FragmentEjerciciosBinding
import net.azarquiel.appgym.model.Ejercicio
import net.azarquiel.appgym.model.Rutina


class EjerciciosFragment(rutina: Rutina) :  DialogFragment() {

    private lateinit var adapter: EjercicioAdapter
    private lateinit var binding: FragmentEjerciciosBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var datosUserSH: SharedPreferences
    private var email: String? = null
    private var rutina: Rutina = rutina
    private lateinit var ejs: MutableList<Ejercicio>

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
    }
    private fun initRV() {
        email = datosUserSH.getString("email", "")
        adapter = EjercicioAdapter(requireContext(), R.layout.rowej,onClickListener)
        ejs = mutableListOf<Ejercicio>()
        binding.rvejs.adapter=adapter
        binding.rvejs.layoutManager= LinearLayoutManager(requireContext())
        obtenerEjs()
        binding.fabaaddej.setOnClickListener {
            añadirej()
        }
    }

    private val onClickListener = object : EjercicioAdapter.OnClickListenerRecycler {
        override fun OnClickEliminarEj(itemView: View) {
            val ejercicio = itemView.tag as Ejercicio // Obtener el objeto Comida asociado al itemView
            val position = ejs.indexOf(ejercicio) // Encontrar la posición del elemento en el array de comidas
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
                        val rutinas = document.data?.get("rutinas") as? MutableMap<String, MutableList<Map<String, Any>>>
                        if (rutinas != null) {
                            rutinas.forEach { (_, ejercicios) ->
                                ejercicios?.let {
                                    val ejercicioMap = ejercicios.find { it["NombreEj"] == ejercicio.NombreEj } // Encuentra el ejercicio en la lista de ejercicios
                                    if (ejercicioMap != null) {
                                        ejercicios.remove(ejercicioMap) // Elimina el ejercicio de la lista
                                        rutinasdb.update("rutinas", rutinas) // Actualiza la lista de ejercicios en la base de datos
                                            .addOnSuccessListener {
                                                // Ejercicio eliminado con éxito
                                            }
                                            .addOnFailureListener { e ->
                                                // Manejar errores al actualizar la lista de ejercicios en la base de datos
                                            }
                                    }
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
                rutinasdb.get()
                    .addOnSuccessListener { document ->
                        val rutinas = document.data?.get("rutinas") as? MutableMap<String, Any>
                        if (rutinas != null) {
                            val ejercicios= rutinas[rutina.Nombre] as? MutableList<Ejercicio>
                            if (!ejercicios.isNullOrEmpty()){
                                ejs.clear()
                                documentToListEjs(ejercicios)
                                adapter.setEjercicios(ejs)
                                adapter.notifyDataSetChanged()
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                    }
            }
        }
    }
    private fun añadirej() {
        val AddEjFragment = AddEjFragment(rutina)
        AddEjFragment.setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogTheme)
        AddEjFragment.show(childFragmentManager, "AddEjFragment.TAG")
    }
    private fun documentToListEjs(ejs: MutableList<Ejercicio>) {
        ejs.forEach { ejercicio ->
            this.ejs.add(ejercicio)
        }
    }
}