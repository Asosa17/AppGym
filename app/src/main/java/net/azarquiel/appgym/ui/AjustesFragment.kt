package net.azarquiel.appgym.ui

import android.Manifest
import android.animation.Animator
import android.animation.ValueAnimator
import android.transition.TransitionManager
import android.app.Activity
import android.app.Dialog
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.health.connect.datatypes.units.Length
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.transition.Fade
import android.transition.Visibility
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.animation.doOnEnd
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.recreate
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.os.LocaleListCompat
import androidx.core.view.get
import androidx.core.view.marginBottom
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.azarquiel.appgym.R
import net.azarquiel.appgym.databinding.FragmentAjustesBinding
import net.azarquiel.appgym.model.User
import net.azarquiel.appgym.view.MainActivity
import net.azarquiel.appgym.view.PrincipalActivity
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AjustesFragment : Fragment() {
    private lateinit var UriTemp: Uri
    private lateinit var tvpesouser: TextView
    private lateinit var tvAlturauser: TextView
    private lateinit var UserLocal: User
    private lateinit var animator: ValueAnimator
    private var startHeight: Int = 0
    private var startHeight2: Int = 0
    private var visibility: Int = View.GONE
    private lateinit var datos: ConstraintLayout
    private lateinit var clajustes: ConstraintLayout
    private lateinit var flecha: ImageView
    private lateinit var binding: FragmentAjustesBinding
    private lateinit var imageUrl: String
    private lateinit var email: String
    private lateinit var dialog: Dialog
    private var userlocal: FirebaseUser?=null
    private lateinit var IvUserAvatar: ImageView
    private lateinit var launcherImage : ActivityResultLauncher<Intent>
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var root: View
    private lateinit var btnCamara: Button
    private lateinit var btnGalery: Button
    private val CAMERA_PERMISSION_REQUEST_CODE = 100
    private lateinit var datosUserSH: SharedPreferences
    private var isExpanded = false
    private var isExpanded1 = false
    private var isExpanded2 = false
    private var isExpanded3 = false
    private var isExpanded4 = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAjustesBinding.inflate(inflater, container, false)
        val root = binding.root
        // Calcular startHeight utilizando datos1aj
        root.post {
            startHeight = binding.datos1aj.height
        }
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        datosUserSH = requireActivity().getSharedPreferences("datosUserSh", MODE_PRIVATE)
        getUser()
        val imageUrl = datosUserSH.getString("imageUrl", null)
        val username = datosUserSH.getString("username",null)
        email = datosUserSH.getString("email",null).toString()

        if (imageUrl.equals("null")||imageUrl.isNullOrEmpty()) {
            binding.IvUserAvatar.setImageResource(R.drawable.noimage)
        }else{
            Picasso.get().load(imageUrl).into(binding.IvUserAvatar)
        }
        if (!username.isNullOrEmpty()) {
            binding.tvNombreUsuarioAjustes.setText(username)
        }
        tvAlturauser=binding.tvAlturauser
        tvpesouser=binding.tvpesouser

        allOnclicks()

        onResultImage()
    }
    private fun getUser() {
        val currentUser = auth.currentUser
        userlocal = currentUser!!
        if (userlocal != null) {
            val userDocument = userlocal?.email?.let { db.collection("users").document(it) }
            userDocument!!.get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val altura = document.getString("altura") ?: ""
                        tvAlturauser.text = Editable.Factory.getInstance().newEditable(altura)
                        // Obtener el mapa de pesos del documento si existe
                        val pesoMap = document["peso"] as? Map<String, Any>
                        if (pesoMap != null) {
                            val keys = pesoMap.keys.sortedDescending()
                            val lastKey = keys.firstOrNull()
                            if (lastKey != null) {
                                val lastPeso = pesoMap[lastKey].toString()
                                tvpesouser.text = Editable.Factory.getInstance().newEditable(lastPeso)
                            } else {
                                // No hay ningún peso registrado
                                tvpesouser.text = Editable.Factory.getInstance().newEditable("")
                            }
                        } else {
                            // No hay datos de peso en Firebase
                            tvpesouser.text = Editable.Factory.getInstance().newEditable("")
                        }
                    } else {

                    }
                }
                .addOnFailureListener { e ->
                    // Maneja cualquier error que pueda ocurrir al realizar la consulta
                    Log.w(ContentValues.TAG, "Error al obtener datos del usuario: ", e)
                }
        } else {
            // El usuario no está autenticado, maneja esta situación según tus requerimientos
        }
    }
    private fun allOnclicks() {
        binding.IvUserAvatar.setOnClickListener {
            if (userlocal != null) {
                openDialog()
            } else {
                msg("Inicia sesión o regístrate")
            }
        }
        binding.datos1aj.setOnClickListener {
            visibility=binding.cl1ajustes.visibility
            expandir(1,visibility)
            isExpanded1 = !isExpanded1
        }

        binding.datos2aj.setOnClickListener {
            visibility=binding.clajustes2.visibility
            expandir(2,visibility)
            isExpanded2 = !isExpanded2
        }

        binding.datos3aj.setOnClickListener {
            visibility=binding.clajsutes3.visibility
            expandir(3,visibility)
            isExpanded3 = !isExpanded3
        }

        binding.datos4aj.setOnClickListener {
            visibility=binding.clajsutes4.visibility
            expandir(4,visibility)
            isExpanded4 = !isExpanded4
        }
        binding.ivsaveajustes.setOnClickListener {
            guardarStats()
        }

        binding.btncerrarsesion.setOnClickListener {
            logOut()
            val intent=Intent(requireContext(), MainActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
        binding.ivesp.setOnClickListener{
            actualizarIdioma("es")
        }
        binding.iving.setOnClickListener{
            actualizarIdioma("en")
        }
        binding.ivfran.setOnClickListener{
            actualizarIdioma("fr")
        }
        binding.ivtemarojo.setOnClickListener{
            cambiartema(1)
        }
        binding.ivtemaaazul.setOnClickListener{
            cambiartema(2)
        }
        binding.ivtemaverde.setOnClickListener{
            cambiartema(3)
        }
    }
    private fun guardarStats() {
        if (tvAlturauser.text.toString().isEmpty() || tvpesouser.text.toString().isEmpty()) {
            Toast.makeText(requireActivity(), "Rellena bien los campos", Toast.LENGTH_LONG).show()
            return
        }
        // Obtener la fecha actual en el formato deseado (por ejemplo, "2022-01-01")
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val tvAlturauser = tvAlturauser.text.toString()
        val tvpesouser = tvpesouser.text.toString()
        // Obtener el documento actual del usuario
        val userDocumentRef = db.collection("users").document(email)
        userDocumentRef.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {
                // Obtener el mapa de pesos actual
                val pesoMap = documentSnapshot.data?.get("peso") as? MutableMap<String, Any> ?: HashMap()

                // Agregar el nuevo peso al mapa
                pesoMap[currentDate] = tvpesouser

                // Actualizar la colección "users" en Firestore con la altura y el mapa de pesos actualizado
                val dataToUpdate: MutableMap<String, Any> = HashMap()
                dataToUpdate["altura"] = tvAlturauser
                dataToUpdate["peso"] = pesoMap

                // Actualizar el documento en Firestore
                userDocumentRef.update(dataToUpdate)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Datos guardados", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Log.w("TAG", "Error al actualizar el documento", e)
                        Toast.makeText(requireContext(), "Error al guardar los datos", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Log.d("TAG", "El documento del usuario no existe")
                Toast.makeText(requireContext(), "El usuario no existe", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { e ->
            Log.w("TAG", "Error al obtener el documento del usuario", e)
            Toast.makeText(requireContext(), "Error al obtener los datos del usuario", Toast.LENGTH_SHORT).show()
        }
    }

    private fun cambiartema(tema:Int) {
        var editor = datosUserSH.edit()
        val intent = Intent(requireContext(), MainActivity::class.java)
        when(tema){
            1-> {
                requireActivity().setTheme(R.style.Base_Theme_TemaApp1)
                editor.putString("tema", "1")
                editor.apply()
            }
            2->{
                requireActivity().setTheme(R.style.Theme_TemaApp2)
                editor.putString("tema", "2")
                editor.apply()
            }
            3->{
                requireActivity().setTheme(R.style.Theme_TemaApp3)
                editor.putString("tema", "3")
                editor.apply()
            }
        }
        requireActivity().finish()
        startActivity(intent)
    }

    private fun actualizarIdioma(idioma:String) {
        val locale = Locale(idioma)
        val config = resources.configuration
        var editor = datosUserSH.edit()
        val intent = Intent(requireContext(), MainActivity::class.java)

        Locale.setDefault(locale)
        config.setLocale(locale)
        // Actualiza el idioma
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(idioma))

        requireActivity().finish()
        startActivity(intent)

        editor.putString("idioma", idioma)
        editor.apply()
    }

    private fun logOut() {
        auth.signOut()
        userlocal=null
        val editor = datosUserSH.edit()
        editor.clear()
        editor.apply()
    }

    private fun expandir(id:Int, visibility: Int) {
        val newVisibility = if (visibility == View.VISIBLE) {
            View.GONE // Si está visible, lo hacemos invisible
        } else {
            View.VISIBLE // Si no está visible, lo hacemos visible
        }
        cerrar2(id)
        when(id){
            1->{
                datos=binding.datos1aj
                flecha=binding.flechaDes1
                isExpanded=isExpanded1
                clajustes=binding.cl1ajustes
            }
            2->{
                datos=binding.datos2aj
                flecha=binding.flechaDes2
                isExpanded=isExpanded2
                clajustes=binding.clajustes2
            }
            3->{
                datos=binding.datos3aj
                flecha=binding.flechaDes3
                isExpanded=isExpanded3
                clajustes=binding.clajsutes3
            }
            4->{
                datos=binding.datos4aj
                flecha=binding.flechaDes4
                isExpanded=isExpanded4
                clajustes=binding.clajsutes4
            }
        }
        // Variable para mantener el estado actual de la animación
        val layoutParams = datos.layoutParams as ConstraintLayout.LayoutParams
        //Cambiar visibilidad del contenido al cerrar o abrir
        cambiarVisibilidadConRetraso(clajustes, newVisibility)
        // Altura inicial y final
        //val startHeight = datos.height
        val endHeight = if (isExpanded) startHeight else startHeight+300

        val duration = 800L // Duración de la animación en milisegundos
        // Crear un ValueAnimator para cambiar la altura del ConstraintLayout
        if (isExpanded){
            animator = ValueAnimator.ofInt(startHeight2+43, endHeight)
        }else{
            animator = ValueAnimator.ofInt(startHeight, endHeight)
        }
        animator.duration = duration
        // Cambiar la rotación de la flecha
        val rotation = if (isExpanded) -90f else 0f
        flecha.animate().rotation(rotation).start()
        // Actualizar la altura del ConstraintLayout durante la animación
        animator.addUpdateListener { animation ->
            val animatedValue = animation.animatedValue as Int
            layoutParams.height = animatedValue
            datos.layoutParams = layoutParams
        }
        // Iniciar la animación
        binding.datos1aj.isClickable=false
        binding.datos2aj.isClickable=false
        binding.datos3aj.isClickable=false
        binding.datos4aj.isClickable=false
        animator.start()
        animator.doOnEnd {
            binding.datos1aj.isClickable=true
            binding.datos2aj.isClickable=true
            binding.datos3aj.isClickable=true
            binding.datos4aj.isClickable=true
        }
        Log.d("expandir"," "+isExpanded)
    }

    private fun cambiarVisibilidadConRetraso(view: View, newVisibility: Int) {
        var delay2=if (isExpanded)200L else 600L
        GlobalScope.launch {
            delay(delay2) // Retrasar la ejecución durante la duración de la animación
            withContext(Dispatchers.Main) {
                // Cambiar la visibilidad en el hilo principal después del retraso
                view.visibility = newVisibility
                startHeight2=datos.height
                datos.setPadding(0,0,0,0)

            }
        }
    }

    private fun cerrar2(id: Int) {
        val expandirIds = arrayOf(1, 2, 3, 4) // IDs de los elementos a expandir

        for (expandirId in expandirIds) {
            if (expandirId != id) {
                // Si el ID actual no es igual al ID del elemento que se va a expandir, cerrar el elemento
                val isExpanded = when (expandirId) {
                    1 -> isExpanded1
                    2 -> isExpanded2
                    3 -> isExpanded3
                    4 -> isExpanded4
                    else -> false
                }

                if (isExpanded) {
                    cerrar(expandirId)
                    when (expandirId) {
                        1 -> isExpanded1 = false
                        2 -> isExpanded2 = false
                        3 -> isExpanded3 = false
                        4 -> isExpanded4 = false
                    }
                }
            }
        }
    }
    private fun cerrar(id: Int) {
        // Cerrar el elemento correspondiente
        when (id) {
            1 -> expandir(1, binding.cl1ajustes.visibility)
            2 -> expandir(2, binding.clajustes2.visibility)
            3 -> expandir(3, binding.clajsutes3.visibility)
            4 -> expandir(4, binding.clajsutes4.visibility)
        }
    }
    private fun openDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_creator, null)

        btnCamara = dialogView.findViewById<Button>(R.id.btnCamara)
        btnGalery = dialogView.findViewById<Button>(R.id.btnGalery)

        btnGalery.setOnClickListener { onClickOpenGalery() }
        btnCamara.setOnClickListener { onClickOpenCamara() }
        builder.setView(dialogView)
            .setTitle("Agregar imagen desde: ")
            .setNegativeButton("Cancelar") { dialog, id ->
                // Acciones a realizar al hacer clic en "Cancelar"
            }

        dialog = builder.create()
        dialog.show()
    }

    fun onClickOpenGalery() {
        selectImage()
    }
    fun onClickOpenCamara() {
        // Verificar si el permiso de la cámara ya está concedido
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
        ) {
            // Si el permiso no está concedido, solicitarlo al usuario
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
        } else {
            // El permiso ya está concedido, puedes iniciar la actividad de la cámara
            dispatchTakePictureIntent()
        }
    }
    private fun uploadImage(imageUri: Uri) {
        val sdf = SimpleDateFormat("yyyy_M_dd_HH_mm_ss", Locale.getDefault())
        val now = Date()
        val filename = sdf.format(now)
        val storageReference = FirebaseStorage.getInstance().getReference("FotosUsers/$filename ${userlocal!!.email}")
        storageReference.putFile(imageUri)
            .addOnSuccessListener { taskSnapshot ->
                storageReference.downloadUrl.addOnSuccessListener { uri ->
                    auth.currentUser?.let { user ->
                        val userUpdates = hashMapOf<String, Any>(
                            "imageUrl" to uri.toString()
                        )
                        meterDatosUser(uri.toString())
                        db.collection("users").document(user.email!!).update(userUpdates)
                            .addOnSuccessListener {
                                Log.d("UploadImage", "URL de imagen guardada en la base de datos")
                            }
                            .addOnFailureListener { e ->
                                Log.e("UploadImage", "Error al guardar URL de imagen en la base de datos: $e")
                            }
                    }
                }
            }
            .addOnFailureListener { e ->
                msg("Error al subir la imagen: ${e.message}")
            }
    }
    private fun selectImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        launcherImage.launch(intent)
    }
    private fun onResultImage() {
        launcherImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                result.data?.let { intent ->
                    intent.data?.let { uri ->
                        binding.IvUserAvatar.setImageURI(uri)
                        uploadImage(uri)
                        (dialog as AlertDialog).dismiss()
                    }
                }
            }
            // Si la captura de imagen es exitosa
            if (result.data?.extras?.containsKey("data") == true) {
                // Obtener la imagen capturada como bitmap
                val imageBitmap = result.data?.extras?.get("data") as Bitmap
                // Establecer la imagen capturada en el ImageView
                binding.IvUserAvatar.setImageBitmap(imageBitmap)
                saveBitmapImage(imageBitmap)
                // Obtener la URI de la imagen capturada
                val tempUri = UriTemp
                // Subir la imagen a Firebase
                uploadImage(tempUri)
                // Cerrar el diálogo
                (dialog as AlertDialog).dismiss()
            }
        }
    }

    private fun msg(s: String) {

    }
    private fun saveBitmapImage(bitmap: Bitmap) {
        val timestamp = System.currentTimeMillis()
        val values = ContentValues()
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        values.put(MediaStore.Images.Media.DATE_ADDED, timestamp)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Images.Media.DATE_TAKEN, timestamp)
            values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/" + getString(R.string.app_name))
            values.put(MediaStore.Images.Media.IS_PENDING, true)
            val uri = requireActivity().contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            if (uri != null) {
                try {
                    val outputStream = requireActivity().contentResolver.openOutputStream(uri)
                    if (outputStream != null) {
                        try {
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                            outputStream.close()
                        } catch (e: Exception) {
                            Log.e(TAG, "saveBitmapImage: ", e)
                        }
                    }
                    values.put(MediaStore.Images.Media.IS_PENDING, false)
                    requireActivity().contentResolver.update(uri, values, null, null)
                    UriTemp=uri
                    Toast.makeText(requireContext(), "Saved...", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Log.e(TAG, "saveBitmapImage: ", e)
                }
            }
        } else {
            val imageFileFolder = File(Environment.getExternalStorageDirectory().toString() + '/' + getString(R.string.app_name))
            if (!imageFileFolder.exists()) {
                imageFileFolder.mkdirs()
            }
            val mImageName = "$timestamp.png"
            val imageFile = File(imageFileFolder, mImageName)
            try {
                val outputStream: OutputStream = FileOutputStream(imageFile)
                try {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    outputStream.close()
                } catch (e: Exception) {
                    Log.e(TAG, "saveBitmapImage: ", e)
                }
                values.put(MediaStore.Images.Media.DATA, imageFile.absolutePath)
                requireActivity().contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                Toast.makeText(requireContext(), "Saved...", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e(TAG, "saveBitmapImage: ", e)
            }
        }
    }
    private fun dispatchTakePictureIntent() {
        // Creamos un intent para capturar una imagen
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // Verificamos si hay una actividad que pueda manejar este intent
        takePictureIntent.resolveActivity(requireActivity().packageManager)?.also {
            // Utilizamos el registro de actividad para el resultado
            launcherImage.launch(takePictureIntent)
        }
    }
    //SharePreferences
    private fun meterDatosUser(imageUrl:String) {
        var editor = datosUserSH.edit()
        editor.putString("imageUrl", imageUrl)
        editor.commit()
    }
}