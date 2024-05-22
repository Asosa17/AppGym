package net.azarquiel.appgym.ui

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.echo.holographlibrary.Line
import com.echo.holographlibrary.LinePoint
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.squareup.picasso.Picasso
import net.azarquiel.appgym.R
import net.azarquiel.appgym.databinding.FragmentEjerciciosBinding
import net.azarquiel.appgym.databinding.FragmentHomeBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HomeFragment : Fragment(), OnChartValueSelectedListener {

    private lateinit var lineChart: LineChart
    private lateinit var binding: FragmentHomeBinding
    private lateinit var root: View
    private var userlocal: FirebaseUser? = null
    private lateinit var auth: FirebaseAuth;
    private lateinit var db: FirebaseFirestore
    private lateinit var datosUserSH: SharedPreferences
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root = binding.root
        return root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = Firebase.auth
        db = Firebase.firestore
        datosUserSH = requireActivity().getSharedPreferences("datosUserSh", Context.MODE_PRIVATE)
        val imageUrl = datosUserSH.getString("imageUrl", null)
        val username = datosUserSH.getString("username",null)
        Picasso.get().load(imageUrl).into(binding.ivhomeuser)
        binding.tvhomeuser.setText("Hola ${username}")
        lineChart = binding.barChart

        obtenePesos()

        lineChart.setOnChartValueSelectedListener(this)
    }

    override fun onValueSelected(e: Entry?, h: Highlight?) {
        e?.let {
            // Acción al seleccionar un valor
            Toast.makeText(context, "Valor seleccionado: ${it.y} en el mes: ${it.x.toInt()}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onNothingSelected() {
        // Acción al deseleccionar un valor
    }
    private fun obtenePesos() {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            val userEmail = user.email
            userEmail?.let { email ->
                val pesosdb = db.collection("users").document(email)
                pesosdb.addSnapshotListener { snapshot, e ->
                        if (e != null) {
                            Log.w("TAG", "Listen failed.", e)
                            return@addSnapshotListener
                        }
                        if (snapshot != null && snapshot.exists()) {
                            val weightsMap = snapshot.data?.get("peso") as Map<String, Any>
                            val monthlyAverages = calculateMonthlyAverages(weightsMap)
                            setupChart(monthlyAverages)
                        }
                    }
            }
        }
    }
    private fun calculateMonthlyAverages(weightsMap: Map<String, Any>): List<Entry> {
        val monthWeights = mutableMapOf<String, MutableList<Double>>()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val monthFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())

        for ((dateString, weightString) in weightsMap) {
            val weight = weightString.toString().toDouble()
            val date = dateFormat.parse(dateString)
            val month = monthFormat.format(date)
            if (monthWeights.containsKey(month)) {
                monthWeights[month]!!.add(weight)
            } else {
                monthWeights[month] = mutableListOf(weight)
            }
        }

        val monthlyAverages = ArrayList<Entry>()
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)

        for (month in 0..11) {
            calendar.set(Calendar.MONTH, month)
            val monthString = monthFormat.format(calendar.time)
            val weights = monthWeights[monthString]
            val averageWeight = weights?.let { it.sum() / it.size } ?: 0.0
            monthlyAverages.add(Entry((month + 1).toFloat(), averageWeight.toFloat()))
        }

        return monthlyAverages
    }

    private fun setupChart(monthlyAverages: List<Entry>) {


        // Agregar puntos ficticios para crear espacio
        val adjustedEntries = mutableListOf<Entry>()
        adjustedEntries.add(Entry(0.5f, 0f)) // Punto ficticio al inicio
        adjustedEntries.addAll(monthlyAverages)
        adjustedEntries.add(Entry(12.5f, 0f)) // Punto ficticio al final

        // Configuración del conjunto de datos
        val dataSet = LineDataSet(adjustedEntries, "Peso promedio mensual")
        dataSet.color = Color.WHITE
        dataSet.circleColors = listOf(Color.BLUE) // Color de los puntos
        dataSet.valueTextColor = Color.WHITE
        dataSet.setDrawFilled(false)
        dataSet.fillColor = Color.WHITE
        lineChart.legend.isEnabled = false

        // Configuración del eje X (Meses)
        val xAxis: XAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.textColor = Color.WHITE
        xAxis.setDrawAxisLine(true)
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f // Saltos entre cada etiqueta de mes
        xAxis.valueFormatter = IndexAxisValueFormatter(
            listOf("","E", "F", "M", "A", "M", "J", "J", "A", "S", "O", "N", "D","")
        )

        val maxWeight = monthlyAverages.maxOfOrNull { it.y } ?: 0f
        // Configuración del eje Y (Pesos en kg)
        val yAxisRight: YAxis = lineChart.axisRight
        yAxisRight.isEnabled = false
        val yAxisLeft: YAxis = lineChart.axisLeft
        yAxisLeft.textColor = Color.WHITE
        yAxisLeft.axisMinimum = 0f
        yAxisLeft.axisMaximum = (maxWeight + 20).coerceAtLeast(100f)
        yAxisLeft.granularity = 20f

        // Configuración del gráfico
        val lineData = LineData(dataSet)
        lineChart.data = lineData
        lineChart.description.isEnabled = false
        lineChart.setNoDataText("No hay datos disponibles")

        // Configuración del comportamiento del gráfico
        lineChart.isDragEnabled = true // Habilitar desplazamiento
        lineChart.isScaleXEnabled = false // Deshabilitar zoom en eje X
        lineChart.isScaleYEnabled = false // Deshabilitar zoom en eje Y
        lineChart.setPinchZoom(false) // Deshabilitar zoom mediante pellizco
        lineChart.isDoubleTapToZoomEnabled = false // Deshabilitar zoom al doble toque

        // Permitir desplazamiento horizontal y establecer el rango visible a 3 meses
        lineChart.setVisibleXRangeMaximum(3f)
        lineChart.moveViewToX(0f)

        lineChart.invalidate() // Refrescar el gráfico
    }

}
