package net.azarquiel.appgym.ui

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.recyclerview.widget.LinearLayoutManager
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
import net.azarquiel.appgym.adapters.PesoAdapter
import net.azarquiel.appgym.adapters.RutinaAdapter
import net.azarquiel.appgym.databinding.FragmentEjerciciosBinding
import net.azarquiel.appgym.databinding.FragmentHomeBinding
import net.azarquiel.appgym.model.Peso
import net.azarquiel.appgym.model.Rutina
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HomeFragment : Fragment(), OnChartValueSelectedListener {

    private lateinit var messelec: TextView
    private lateinit var lineChart: LineChart
    private lateinit var binding: FragmentHomeBinding
    private lateinit var root: View
    private var userlocal: FirebaseUser? = null
    private lateinit var auth: FirebaseAuth;
    private lateinit var db: FirebaseFirestore
    private lateinit var datosUserSH: SharedPreferences
    private lateinit var weightsByMonth: MutableList<Peso>
    private lateinit var adapter: PesoAdapter
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
        messelec=binding.tvmesselecc
        lineChart = binding.barChart
        initRV()
        obtenePesos()

        lineChart.setOnChartValueSelectedListener(this)

    }

    private fun initRV() {
        adapter = PesoAdapter(requireContext(), R.layout.rowpeso)
        weightsByMonth = mutableListOf<Peso>()
        binding.rvpesoshomf.adapter=adapter
        binding.rvpesoshomf.layoutManager= LinearLayoutManager(requireContext())
    }

    override fun onValueSelected(e: Entry?, h: Highlight?) {
        e?.let {
            val month = it.x.toInt()
            val selectedMonthWeights = getWeightsForMonth(month)
            adapter.setPesos(selectedMonthWeights)
            adapter.notifyDataSetChanged()
            messelec.text=obtenernombre(month)

        }
    }

    private fun obtenernombre(month: Int): CharSequence? {
        return when (month) {
            1 -> "Enero"
            2 -> "Febrero"
            3 -> "Marzo"
            4 -> "Abril"
            5 -> "Mayo"
            6 -> "Junio"
            7 -> "Julio"
            8 -> "Agosto"
            9 -> "Septiembre"
            10 -> "Octubre"
            11 -> "Noviembre"
            12 -> "Diciembre"
            else -> ""
        }
    }


    private fun getWeightsForMonth(month: Int): MutableList<Peso> {
        val monthFormat = SimpleDateFormat("MM", Locale.getDefault())
        return weightsByMonth.filter { peso ->
            val pesoMonth = monthFormat.format(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(peso.fecha))
            pesoMonth.toInt() == month
        }.toMutableList()
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
                            val weightsMap = snapshot.data?.get("peso") as Map<String, String>
                            weightsByMonth.clear()
                            weightsMap.forEach { (fecha, pesoStr) ->
                                weightsByMonth.add(Peso(fecha, pesoStr))
                            }
                            weightsByMonth.sortBy { it.fecha }

                            val monthlyAverages = calculateMonthlyAverages(weightsMap)
                            setupChart(monthlyAverages)
                            // Mostrar los pesos del mes actual
                            val currentMonth = Calendar.getInstance().get(Calendar.MONTH)+1
                            val currentMonthWeights = getWeightsForMonth(currentMonth)
                            currentMonthWeights.reverse()
                            adapter.setPesos(currentMonthWeights)
                            adapter.notifyDataSetChanged()
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
        dataSet.mode = LineDataSet.Mode.HORIZONTAL_BEZIER // Configurar la línea como curva
        dataSet.cubicIntensity = 0.2f // Ajusta la intensidad de la curva
        dataSet.setDrawHorizontalHighlightIndicator(false)
        dataSet.highLightColor = Color.GRAY
        dataSet.highlightLineWidth=2f
        dataSet.valueTextSize= 10f
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
        yAxisLeft.setDrawGridLines(false)
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
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH)

        lineChart.moveViewToX(currentMonth.toFloat()-0.5f)
        lineChart.highlightValue(currentMonth.toFloat()+1,0)
        messelec.text=obtenernombre(currentMonth+1)
        lineChart.invalidate() // Refrescar el gráfico
    }

}
