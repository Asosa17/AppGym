package net.azarquiel.appgym.ui

import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import net.azarquiel.appgym.R
import net.azarquiel.appgym.adapters.RutinaAdapter
import net.azarquiel.appgym.databinding.FragmentRutinasBinding
import net.azarquiel.appgym.model.Rutina

class RutinasFragment : Fragment() {

    private lateinit var Rutinas: MutableList<Rutina>
    private lateinit var adapter: RutinaAdapter
    private lateinit var ednombrerutinadialog: EditText
    private lateinit var btnAceptarAddRutina: Button
    private lateinit var datosUserSH: SharedPreferences
    private lateinit var binding: FragmentRutinasBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var dialog: Dialog
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRutinasBinding.inflate(inflater, container, false)
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
        adapter = RutinaAdapter(requireContext(), R.layout.rowrutina,onClickListener)
        Rutinas = mutableListOf<Rutina>()
        binding.rvrutinas.adapter=adapter
        binding.rvrutinas.layoutManager= LinearLayoutManager(requireContext())
        obtenerRutinas()
        binding.fabaaddrutina.setOnClickListener {
            openDialog()
        }

    }

    private val onClickListener = object : RutinaAdapter.OnClickListenerRecycler {
        override fun OnClickRutina(dataItem:Rutina) {
            val EjerciciosFragment = EjerciciosFragment(dataItem)
            EjerciciosFragment.setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogTheme)
            EjerciciosFragment.show(childFragmentManager, "EjerciciosFragment.TAG")
        }
        override fun OnClickEliminarRutina(itemView: View) {
            val rutina = itemView.tag as Rutina // Obtener el objeto Comida asociado al itemView
            val position = Rutinas.indexOf(rutina) // Encontrar la posición del elemento en el array de comidas
            adapter.deleteItem(position) // Eliminar el elemento del RecyclerView y del array de comidas
            adapter.notifyDataSetChanged()
            emilinarrutina(rutina.Nombre)
        }
    }

    private fun emilinarrutina(nombre: String) {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            val userEmail = user.email
            userEmail?.let { email ->
                val rutinasdb = db.collection("users").document(email)
                rutinasdb.get()
                    .addOnSuccessListener { document ->
                        val rutinas = document.data?.get("rutinas") as MutableMap<String,Any>
                        rutinas?.let {
                            // Remover la rutina con el nombre especificado
                            rutinas.remove(nombre)
                            // Actualizar el documento en Firestore sin la rutina eliminada
                            rutinasdb.update("rutinas", rutinas)
                                .addOnSuccessListener {
                                    // Rutina eliminada con éxito
                                }
                                .addOnFailureListener { e ->
                                    // Manejar errores al actualizar el documento en la base de datos
                                }
                        }
                    }
                    .addOnFailureListener { e ->
                        // Manejar errores al obtener la lista de rutinas del usuario desde la base de datos
                    }
            }
        }
    }

    private fun openDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialoaddrutina, null)


        btnAceptarAddRutina = dialogView.findViewById<Button>(R.id.btnAceptarAddRutina)
        ednombrerutinadialog=dialogView.findViewById<EditText>(R.id.ednombrerutinadialog)

        btnAceptarAddRutina.setOnClickListener {
            if (ednombrerutinadialog.text.toString().isNullOrEmpty()){
                Toast.makeText(requireContext(),"Rellene los campos",Toast.LENGTH_SHORT).show()
            }else{
                onClickAceptarAddRutina(ednombrerutinadialog.text.toString())
                dialog.dismiss()
            }
        }
        builder.setView(dialogView)
        dialog = builder.create()
        dialog.show()
    }

    private fun onClickAceptarAddRutina(nombreRutina:String) {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            val userEmail = user.email
            userEmail?.let { email ->
                val rutinasdb = db.collection("users").document(email)
                rutinasdb.get()
                    .addOnSuccessListener { document ->
                        val rutinas = document.data?.get("rutinas") as MutableMap<String,Any>
                        if (!rutinas.containsKey(nombreRutina)){
                            // Crea una nueva rutina vacía
                            val nuevaRutina: MutableMap<String, Any> = mutableMapOf()
                            rutinas[nombreRutina] = nuevaRutina
                            Rutinas.add(Rutina(nombreRutina))
                            adapter.notifyDataSetChanged()
                            rutinasdb.update("rutinas", rutinas)
                                .addOnSuccessListener {
                                }
                                .addOnFailureListener { e ->
                                }
                        }else{
                            Toast.makeText(requireContext(),"Ya existe una rutina con ese nombre", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener { e ->
                        // Manejar errores al obtener la lista de rutinas del usuario desde la base de datos
                    }
            }
        }
    }
    private fun obtenerRutinas() {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            val userEmail = user.email
            userEmail?.let { email ->
                val rutinasdb = db.collection("users").document(email)
                rutinasdb.get()
                    .addOnSuccessListener { document ->
                        val rutinas = document.data?.get("rutinas") as? MutableMap<String, Any>
                        if (rutinas != null) {
                            Rutinas.clear()
                            documentToListRutinas(rutinas)
                            adapter.setRutinas(Rutinas)
                            adapter.notifyDataSetChanged()
                        } else {
                            val nuevoRutinas = mutableMapOf<String, Any>()
                            rutinasdb.update("rutinas", nuevoRutinas)
                                .addOnSuccessListener {
                                }
                                .addOnFailureListener { e ->
                                }
                        }
                    }
                    .addOnFailureListener { e ->
                    }
            }
        }
    }
    private fun documentToListRutinas(rutinas: MutableMap<String, Any>) {
        rutinas.forEach { rutinaMap ->
            val nombre = rutinaMap.key
            val rutina = Rutina(nombre)
            Rutinas.add(rutina)
        }
    }
}