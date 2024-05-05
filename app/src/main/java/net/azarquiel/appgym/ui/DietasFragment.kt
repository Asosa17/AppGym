package net.azarquiel.appgym.ui

import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import net.azarquiel.appgym.R
import net.azarquiel.appgym.adapters.ComidaAdapter
import net.azarquiel.appgym.databinding.FragmentDietasBinding
import net.azarquiel.appgym.model.Comida
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class DietasFragment : Fragment(), ComidaAdapter.OnClickListenerRecycler {

    private lateinit var datosUserSH: SharedPreferences
    private lateinit var btnaceptarcomida: Button
    private lateinit var eddnombrecomida: EditText
    private lateinit var eddcantidad: EditText
    private lateinit var eddkcal100: EditText
    private var suma: Float = 0F
    private lateinit var comidas: MutableList<Comida>
    private lateinit var adapter: ComidaAdapter
    private lateinit var binding: FragmentDietasBinding
    private var selectedDate: Date? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var dialog: Dialog
    private var lastClickedPosition: Int = RecyclerView.NO_POSITION

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDietasBinding.inflate(inflater, container, false)
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
        adapter = ComidaAdapter(requireContext(), R.layout.rowcomida,onClickListener)
        comidas = mutableListOf<Comida>()
        binding.rvdietas.adapter=adapter
        binding.rvdietas.layoutManager=LinearLayoutManager(requireContext())

        val currentDate = Calendar.getInstance().time
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val formattedDate = sdf.format(currentDate)

        binding.tvfecha.text=formattedDate
        obtenerComidasDelDia(currentDate)

        binding.buttoncalendar.setOnClickListener {
            showDatePickerDialog()
        }
        binding.btnaAdircomida.setOnClickListener {
            añadircomida()
        }




    }
    private val onClickListener = object : ComidaAdapter.OnClickListenerRecycler {

        override fun OnClickComida(itemView: View, btneliminarcomidadf: Button) {
            val comida = itemView.tag as Comida // Obtener el objeto Comida asociado al itemView
            val position = comidas.indexOf(comida) // Encontrar la posición del elemento en el array de comidas

            if (lastClickedPosition != RecyclerView.NO_POSITION && lastClickedPosition != position) {
                // Si hay un elemento previamente seleccionado y es diferente al actual, oculta el botón asociado a ese elemento
                val lastView = binding.rvdietas.findViewHolderForAdapterPosition(lastClickedPosition)?.itemView
                val lastButton = lastView?.findViewById<Button>(R.id.btneliminarcomidadf)
                lastButton?.visibility = View.GONE
            }

            // Alterna la visibilidad del botón entre VISIBLE y GONE en función de su estado actual
            if (btneliminarcomidadf.visibility == View.VISIBLE) {
                btneliminarcomidadf.visibility = View.GONE
            } else {
                btneliminarcomidadf.visibility = View.VISIBLE
            }

            lastClickedPosition = position // Actualiza la última posición clicada
        }
        override fun onClickEliminar(itemView: View) {
            val comida = itemView.tag as Comida // Obtener el objeto Comida asociado al itemView
            val position = comidas.indexOf(comida) // Encontrar la posición del elemento en el array de comidas
            adapter.deleteItem(position) // Eliminar el elemento del RecyclerView y del array de comidas
            emilinarcomida(position+1,comida.id)
            suma-=comida.KcalTotales.toFloat()
            binding.tvkcaltotalessuma.text="%.2f".format(suma).replace(".", ",")
        }
    }
    private fun showDatePickerDialog() {
        val materialDatePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(getString(R.string.df_calendarTitle)) // Obtener el texto del recurso de cadena
            .setSelection(selectedDate?.time ?: System.currentTimeMillis()) // Establecer la fecha seleccionada previamente o la fecha de hoy como predeterminada
            .setTheme(R.style.ThemeOverlay_App_DatePicker)
            .build()

        materialDatePicker.addOnPositiveButtonClickListener { selection ->
            val calendar = Calendar.getInstance().apply {
                timeInMillis = selection
            }
            selectedDate = calendar.time
            updateSelectedDateTextView(selectedDate!!)
            obtenerComidasDelDia(selectedDate!!)
            comidas.clear()
            suma=0F
        }

        materialDatePicker.show(requireActivity().supportFragmentManager, "DatePickerDialogTag")
    }
    private fun updateSelectedDateTextView(date: Date) {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val formattedDate = sdf.format(date)
        binding.tvfecha.text = formattedDate

    }
    private fun obtenerComidasDelDia(fechaSeleccionada: Date) {
        // Formatear la fecha seleccionada
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val fechaFormateada = sdf.format(fechaSeleccionada)
        // Obtener el usuario actualmente autenticado
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            val userEmail = user.email
            userEmail?.let { email ->
                // Referencia al documento del usuario
                val userDocument = db.collection("users").document(email)
                // Obtener el mapa de comidas del documento del usuario
                userDocument.get()
                    .addOnSuccessListener { document ->
                        if (document != null) {
                            val comidasMapa = document.data?.get("comidas") as Map<String, Any>?
                            if (comidasMapa != null) {
                                val comidasFecha = comidasMapa[fechaFormateada] as Map<String, Any>?
                                if (comidasFecha!=null){
                                    comidasFecha.forEach { (_, datosComida) ->
                                        val comida = datosComida as Map<String, Any>
                                        val nombreComida = comida["NombreComida"] as String
                                        val cantidad = comida["Cantidad"] as String
                                        val kcal100 = comida["Kcal100"] as String
                                        val kcalTotales = comida["KcalTotales"] as String
                                        val id=comida["id"]as String
                                        suma+=kcalTotales.toFloat()
                                        comidas.add(Comida(nombreComida, cantidad, kcal100, kcalTotales,id))
                                    }
                                    adapter.setComidas(comidas)
                                    binding.tvkcaltotalessuma.text="%.2f".format(suma).replace(".", ",")
                                    adapter.notifyDataSetChanged()
                                }else {
                                    comidas.clear()
                                    suma=0F
                                    binding.tvkcaltotalessuma.text="%.2f".format(suma).replace(".", ",")
                                    adapter.setComidas(comidas)
                                    adapter.notifyDataSetChanged()
                                    Toast.makeText(requireContext(),"No exiten registros para este dia",Toast.LENGTH_SHORT).show()
                                }
                            }

                        }
                    }
                    .addOnFailureListener { exception ->
                        // Manejar errores
                        Log.e("DietasFragment", "Error al obtener comidas del día", exception)
                    }
            }
        }
    }
    private fun añadircomida() {
        dialogañadircomida()
    }

    private fun dialogañadircomida() {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialogcomida, null)

        eddnombrecomida  = dialogView.findViewById<EditText>(R.id.eddnombrecomida)
        eddcantidad = dialogView.findViewById<EditText>(R.id.eddcantidad)
        eddkcal100 = dialogView.findViewById<EditText>(R.id.eddkcal100)
        btnaceptarcomida = dialogView.findViewById<Button>(R.id.btnaceptarcomida)


        btnaceptarcomida.setOnClickListener {
            var cantidaddialog= eddcantidad.text.toString()
            var kcal100dialog=eddkcal100.text.toString()
            var kcaltotalesdialog=(cantidaddialog.toFloat()*kcal100dialog.toFloat())/100f
            var comida=Comida(eddnombrecomida.text.toString(), eddcantidad.text.toString(), eddkcal100.text.toString(), "%.2f".format(kcaltotalesdialog).replace(".", ","),(comidas.count()+1).toString())

            comidas.add(0,comida)
            guardarcomida(comida)

            suma+=kcaltotalesdialog
            binding.tvkcaltotalessuma.text="%.2f".format(suma).replace(".", ",")

            adapter.setComidas(comidas)
            adapter.notifyDataSetChanged()
            dialog.dismiss()
        }


        builder.setView(dialogView)
        dialog = builder.create()
        dialog.show()
    }

    private fun emilinarcomida(position:Int,id:String) {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val currentDate = Calendar.getInstance().time
        var fechaFormateada = " "
        if (selectedDate!=null){
            fechaFormateada = sdf.format(selectedDate)
        }else{
            fechaFormateada=sdf.format(currentDate)
        }
        var posicion=position
        // Obtener el usuario actualmente autenticado
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            val userEmail = user.email
            userEmail?.let { email ->
                // Referencia al documento del usuario
                val userDocument = db.collection("users").document(email)
                userDocument.get()
                    .addOnSuccessListener { document ->
                        if (document != null) {
                            val comidasMapa = document.data?.get("comidas") as MutableMap<String, Any>?
                            if (comidasMapa != null) {
                                // Obtener el mapa de comidas para la fecha actual
                                val comidasFecha = comidasMapa[fechaFormateada] as MutableMap<String, Any>?
                                comidasFecha?.let {
                                    // Buscar la comida en el mapa de comidas y eliminarla
                                    val comidaId = "Comida${id}"
                                    it.remove(comidaId)
                                    // Actualizar el mapa de comidas en Firestore
                                    userDocument.update("comidas", comidasMapa)
                                        .addOnSuccessListener {
                                            // La comida se eliminó correctamente de Firebase
                                            Toast.makeText(requireContext(), "Comida eliminada correctamente", Toast.LENGTH_SHORT).show()
                                        }
                                        .addOnFailureListener { exception ->
                                            // Manejar errores al eliminar la comida de Firebase
                                            Log.e("DietasFragment", "Error al eliminar la comida de Firebase", exception)
                                        }
                                }
                            }
                        }
                    }
                    .addOnFailureListener { exception ->
                        // Manejar errores al obtener el mapa de comidas
                        Log.e("DietasFragment", "Error al obtener comidas del usuario", exception)
                    }
            }
         }
    }
    private fun guardarcomida(comida:Comida) {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val currentDate = Calendar.getInstance().time
        var fechaFormateada = ""
        if (selectedDate!=null){
            fechaFormateada = sdf.format(selectedDate)
        }else{
            fechaFormateada=sdf.format(currentDate)
        }

        val currentUser = auth.currentUser
        currentUser?.let { user ->
            val userEmail = user.email
            userEmail?.let { email ->
                // Referencia al documento del usuario
                val userDocument = db.collection("users").document(email)
                userDocument.get()
                    .addOnSuccessListener { document ->
                        if (document != null) {
                            val comidasMapa = document.data?.get("comidas") as MutableMap<String, Any>?
                            if (comidasMapa != null) {
                                // Obtener el mapa de comidas para la fecha actual o crear uno nuevo si no existe
                                val comidasFecha = comidasMapa.getOrPut(fechaFormateada) { mutableMapOf<String, Any>() } as MutableMap<String, Any>
                                // Generar un ID único para la nueva comida
                                val comidaId = "Comida"+(comidas.count()).toString()
                                // Guardar los datos de la nueva comida en el mapa de comidas
                                comidasFecha[comidaId] = mapOf(
                                    "NombreComida" to comida.NombreComida,
                                    "Cantidad" to comida.Cantidad,
                                    "Kcal100" to comida.Kcal100,
                                    "KcalTotales" to comida.KcalTotales,
                                    "id" to comidas.count().toString()
                                )
                                // Actualizar el mapa de comidas en Firestore
                                userDocument.update("comidas", comidasMapa)
                                    .addOnSuccessListener {
                                        // La comida se guardó exitosamente
                                        Toast.makeText(requireContext(), "Comida guardada correctamente", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener { exception ->
                                        // Manejar errores al guardar la comida
                                        Log.e("DietasFragment", "Error al guardar la comida", exception)
                                    }
                            }
                        }
                    }
                    .addOnFailureListener { exception ->
                        // Manejar errores al obtener el mapa de comidas
                        Log.e("DietasFragment", "Error al obtener comidas del usuario", exception)
                    }
            }
        }
    }
}



