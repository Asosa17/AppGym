package net.azarquiel.appgym.ui

import AddPostFragment
import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import net.azarquiel.appgym.R
import net.azarquiel.appgym.adapters.ComidaAdapter
import net.azarquiel.appgym.adapters.PostAdapter
import net.azarquiel.appgym.databinding.FragmentChatBinding
import net.azarquiel.appgym.databinding.FragmentDietasBinding
import net.azarquiel.appgym.model.Comida
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ChatFragment : Fragment() {

    private var cont: Int = 0
    private lateinit var datosUserSH: SharedPreferences
    private lateinit var btnaceptarcomida: Button
    private lateinit var eddnombrecomida: EditText
    private lateinit var eddcantidad: EditText
    private lateinit var eddkcal100: EditText
    private lateinit var adapter:  PostAdapter
    private lateinit var binding: FragmentChatBinding
    private var selectedDate: Date? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var formattedDate: String
    private var lastClickedPosition: Int = RecyclerView.NO_POSITION

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChatBinding.inflate(inflater, container, false)
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
//        adapter = ComidaAdapter(requireContext(), R.layout.rowcomida,onClickListener)
//        comidas = mutableListOf<Comida>()
//        binding.rvdietas.adapter=adapter
//        binding.rvdietas.layoutManager= LinearLayoutManager(requireContext())
//
        val currentDate = Calendar.getInstance().time
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        formattedDate = sdf.format(currentDate)

        binding.fabaAdirpost.setOnClickListener {
            replaceFragment(AddPostFragment())
        }
    }
    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager=requireActivity().supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.FrameLayout, fragment)
        fragmentTransaction.commit()
    }
}