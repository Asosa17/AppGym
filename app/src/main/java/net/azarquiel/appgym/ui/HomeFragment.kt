package net.azarquiel.appgym.ui

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.echo.holographlibrary.Line
import com.echo.holographlibrary.LinePoint
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
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

class HomeFragment : Fragment() {

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

        var lineChart=binding.barChart

        // Datos para el gráfico
        val entries = listOf(
            Entry(.5f, 0f),
            Entry(1f, 20f),
            Entry(2f, 15f),
            Entry(3f, 25f),
            Entry(4f, 30f),
            Entry(5f, 30f),
            Entry(6f, 70f),
            Entry(7f, 30f),
            Entry(8f, 30f),
            Entry(9f, 30f),
            Entry(10f, 30f),
            Entry(11f, 18f),
            Entry(12f, 18f),
            Entry(12.5f,0f)
        )

        // Configuración del conjunto de datos
        val dataSet = LineDataSet(entries, "Datos de ejemplo")
        dataSet.color = Color.WHITE
        dataSet.circleColors = listOf(Color.BLUE) // Color de los puntos
        dataSet.valueTextColor = Color.WHITE
        dataSet.fillColor = Color.WHITE
        lineChart.legend.isEnabled = false

        /// Configuración del eje X (Meses)
        val xAxis: XAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.textColor = Color.WHITE
        xAxis.setDrawAxisLine(true)
        xAxis.axisLineWidth= 3f
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f // Saltos entre cada etiqueta de mes
        xAxis.valueFormatter = IndexAxisValueFormatter(
            listOf("","E", "F", "M", "A", "M", "J", "J", "A", "S", "O", "N", "D")
        )

        // Configuración del eje Y (Pesos en kg de 20 en 20 hasta 140)
        val yAxisRight: YAxis = lineChart.axisRight
        yAxisRight.isEnabled = false
        val yAxisLeft: YAxis = lineChart.axisLeft
        yAxisLeft.textColor = Color.WHITE
        yAxisLeft.axisMinimum = 0f
        yAxisLeft.axisMaximum = 140f
        yAxisLeft.granularity = 20f
        yAxisLeft.gridColor = Color.LTGRAY
        yAxisLeft.gridLineWidth = .1f // Cambiar el grosor de las líneas de cuadrícula
        yAxisLeft.axisLineWidth= 3f
        yAxisLeft.enableGridDashedLine(10f,10f,1f)

        // Configuración del gráfico
        val lineData = LineData(dataSet)
        lineChart.data = lineData
        lineChart.description.isEnabled = false
        lineChart.setNoDataText("No hay datos disponibles")



        lineChart.isDragXEnabled= true
        lineChart.isDragYEnabled= false
        lineChart.setPinchZoom(false)
        lineChart.isScaleXEnabled = false // Deshabilitar zoom en eje X
        lineChart.isScaleYEnabled = false // Deshabilitar zoom en eje Y
        lineChart.isDoubleTapToZoomEnabled = false // Deshabilitar zoom al doble toque

        // Permitir desplazamiento horizontal
        lineChart.setVisibleXRangeMaximum(3f) // Ajustar el rango visible máximo en el eje X
        lineChart.moveViewToX(0f)

        lineChart.invalidate()
    }


}