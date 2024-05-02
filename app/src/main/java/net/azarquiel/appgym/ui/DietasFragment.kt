package net.azarquiel.appgym.ui

import android.app.DatePickerDialog
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Paint.Style
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.runBlocking
import net.azarquiel.appgym.R
import net.azarquiel.appgym.adapters.ComidaAdapter
import net.azarquiel.appgym.databinding.FragmentDietasBinding
import net.azarquiel.appgym.model.Comida
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone


class DietasFragment : Fragment()  {

    private var suma: Int = 0
    private lateinit var comidas: MutableList<Comida>
    private lateinit var adapter: ComidaAdapter
    private lateinit var binding: FragmentDietasBinding
    private var selectedDate: Date? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

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
        initRV()

    }

    private fun initRV() {
        adapter = ComidaAdapter(requireContext(), R.layout.rowcomida)
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
            suma=0
        }

        materialDatePicker.show(requireActivity().supportFragmentManager, "DatePickerDialogTag")
    }


    private fun updateSelectedDateTextView(date: Date) {
        // Formatear la fecha y la muestra en el TextView
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
                                        suma+=kcalTotales.toInt()
                                        comidas.add(Comida(nombreComida, cantidad, kcal100, kcalTotales))
                                    }
                                    adapter.setComidas(comidas)
                                    binding.tvkcaltotalessuma.text=suma.toString()
                                    adapter.notifyDataSetChanged()
                                }else {
                                    comidas.clear()
                                    suma=0
                                    binding.tvkcaltotalessuma.text=suma.toString()
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
        comidas.add(Comida(" ", "  ", " ", " "))
        adapter.notifyDataSetChanged()
    }
}


