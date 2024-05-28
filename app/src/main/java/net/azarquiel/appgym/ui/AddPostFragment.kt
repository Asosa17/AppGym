import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.azarquiel.appgym.R
import net.azarquiel.appgym.adapters.PostAdapter
import net.azarquiel.appgym.databinding.FragmentAddPostBinding
import net.azarquiel.appgym.databinding.FragmentChatBinding
import net.azarquiel.appgym.model.Comentario
import net.azarquiel.appgym.model.Comida
import net.azarquiel.appgym.model.Post
import net.azarquiel.appgym.ui.ChatFragment
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddPostFragment(val Chat:ChatFragment) : DialogFragment() {

    private var postschat: MutableList<Post> = Chat.posts
    private var UriTemp: Uri? = null
    private lateinit var bottomSheetDialog: BottomSheetDialog
    private lateinit var binding: FragmentAddPostBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var formattedDate: String
    private lateinit var datosUserSH: SharedPreferences
    private  var newDocument: MutableMap<String, Any> = HashMap()
    private lateinit var post: Post
    private lateinit var formattedDate2: String
    private var email: String? = null
    private var username: String? = null
    private lateinit var coment: String
    private  var foto:String = ""
    private lateinit var btnCamara: Button
    private lateinit var btnGalery: Button
    private val CAMERA_PERMISSION_REQUEST_CODE = 100
    private lateinit var launcherImage : ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddPostBinding.inflate(inflater, container, false)
        val root = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        datosUserSH = requireActivity().getSharedPreferences("datosUserSh", Context.MODE_PRIVATE)
        initRV()
        onResultImage()
    }
    private fun initRV() {
        val currentDate = Calendar.getInstance().time
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        formattedDate = sdf.format(currentDate)
        email = datosUserSH.getString("email", "")
        username = datosUserSH.getString("username", "")

        binding.btnpublicaraddpost.setOnClickListener {
            coment = binding.edappiecoment.text.toString()
            val sdf2 = SimpleDateFormat("ddMMyyyy", Locale.getDefault())
            formattedDate2 = sdf2.format(currentDate)
            post = Post(foto, mutableListOf(), mutableListOf(), coment, username.toString(), formattedDate,"" )

            newDocument["Likes"] = post.Likes
            newDocument["Comentarios"] = post.Comentarios
            newDocument["PieComent"] = post.PieComent
            newDocument["Usuario"] = post.Usuario
            newDocument["Fecha"] = post.Fecha

            publicarPost()
            publicarPostTotales()

            if (post.Foto.isNotBlank()&&post.PieComent.isNotBlank()){
                GlobalScope.launch {
                    delay(1000)
                    withContext(Dispatchers.Main) {
                        Chat.refresh(postschat,formattedDate2)
                        dismiss()
                    }
                }
            }


        }
        binding.btncancelaraddpost.setOnClickListener {
            dismiss()
        }
        binding.ivcancelaraddpost.setOnClickListener {
            dismiss()
        }
        binding.ivapfotopost.setOnClickListener {
            openDialog()
        }
        val tema = datosUserSH.getString("tema","1").toString()
        detectafondo(tema)
    }
    private fun detectafondo(tema: String) {
        val tema = tema.toInt()
        when(tema){
            1-> {
                binding.cnladdpost.setBackgroundResource(R.drawable.fondoajustes)
            }
            2->{
                binding.cnladdpost.setBackgroundResource(R.drawable.fondoajustesazul)
            }
            3->{
                binding.cnladdpost.setBackgroundResource(R.drawable.fondoajustesverde)
            }

        }
    }

    private fun publicarPost() {
        email?.let { userEmail ->
            // Referencia al documento del usuario
            val posts=db.collection("posts").document(formattedDate2).collection("posts")
            posts.get()
                .addOnSuccessListener { collection ->
                    if (!collection.isEmpty) {
                        var postid=" "
                        if (collection.count()>8){
                            postid = "post"+"${(collection.count()+1)}"+"_${email}"
                        } else if(collection.count()<=8) {
                            postid = "post0"+"${(collection.count()+1)}"+"_${email}"
                        }
                        newDocument["id"] = postid
                        if (post.Foto.isBlank()||post.PieComent.isBlank()){
                            Toast.makeText(requireContext(),"Termine el post para publicar",Toast.LENGTH_SHORT).show()
                        }else{
                            posts.document(postid).set(newDocument)
                        }
                        post.id=postid
                        postschat.add(0,post)
                    }else{
                        val postid = "post01"+"_${email}"
                        newDocument["id"] = postid
                        if (post.Foto.isBlank()||post.PieComent.isBlank()){
                            Toast.makeText(requireContext(),"Termine el post para publicar",Toast.LENGTH_SHORT).show()
                        }else{
                            posts.document(postid).set(newDocument)
                        }
                        post.id=postid
                        postschat.add(0,post)
                    }
                }
                .addOnFailureListener { exception ->
                    // Manejar errores al obtener el mapa de comidas
                    Log.e("ChatFragment", "Error al obtener posts del usuario", exception)
                }

        }
    }
    private fun publicarPostTotales() {
        email?.let { userEmail ->
            // Referencia al documento del usuario
            val postsTotales=db.collection("postsTotales")
            postsTotales.get()
                .addOnSuccessListener { collection ->
                    if (!collection.isEmpty) {
                        var postid=" "
                        if (collection.count()>8){
                            postid = "post"+"${(collection.count()+1)}"+"_${email}"
                        } else if(collection.count()<=8) {
                            postid = "post0"+"${(collection.count()+1)}"+"_${email}"
                        }
                        newDocument["id"] = postid
                        if (post.Foto.isNotBlank()&&post.PieComent.isNotBlank()){
                            postsTotales.document(postid).set(newDocument)
                        }
                    }else{
                        val postid = "post01"+"_${email}"
                        newDocument["id"] = postid
                        if (post.Foto.isNotBlank()&&post.PieComent.isNotBlank()){
                            postsTotales.document(postid).set(newDocument)
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    // Manejar errores al obtener el mapa de comidas
                    Log.e("ChatFragment", "Error al obtener posts del usuario", exception)
                }

        }
    }
    private fun openDialog() {
        bottomSheetDialog = BottomSheetDialog(requireContext())
        val bottomSheetView = layoutInflater.inflate(R.layout.dialog_creator, null)

        btnCamara = bottomSheetView.findViewById<Button>(R.id.btnCamara)
        btnGalery = bottomSheetView.findViewById<Button>(R.id.btnGalery)

        btnGalery.setOnClickListener { onClickOpenGalery() }
        btnCamara.setOnClickListener { onClickOpenCamara() }

        bottomSheetDialog.setContentView(bottomSheetView)
        bottomSheetDialog.show()
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
    private fun uploadImage(imageUri: Uri?) {
        val sdf = SimpleDateFormat("yyyy_M_dd_HH_mm_ss", Locale.getDefault())
        val now = Date()
        val filename = sdf.format(now)
        val storageReference = FirebaseStorage.getInstance().getReference("posts/$filename ${email}")
        storageReference.putFile(imageUri!!)
            .addOnSuccessListener { taskSnapshot ->
                storageReference.downloadUrl.addOnSuccessListener { uri ->
                    newDocument["Foto"] = uri.toString()
                    foto = uri.toString()
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
                        binding.ivapfotopost.setImageURI(uri)
                        uploadImage(uri)
                        bottomSheetDialog.dismiss()
                    }
                }
            }
            // Si la captura de imagen es exitosa
            if (result.data?.extras?.containsKey("data") == true) {
                // Obtener la imagen capturada como bitmap
                val imageBitmap = result.data?.extras?.get("data") as Bitmap
                // Establecer la imagen capturada en el ImageView
                binding.ivapfotopost.setImageBitmap(imageBitmap)
                // Obtener la URI de la imagen capturada
                saveBitmapImage(imageBitmap)
                val tempUri = UriTemp
                // Subir la imagen a Firebase
                uploadImage(tempUri)
                // Cerrar el diálogo
                bottomSheetDialog.dismiss()
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
                            Log.e(ContentValues.TAG, "saveBitmapImage: ", e)
                        }
                    }
                    values.put(MediaStore.Images.Media.IS_PENDING, false)
                    requireActivity().contentResolver.update(uri, values, null, null)
                    UriTemp=uri
                    Toast.makeText(requireContext(), "Saved...", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Log.e(ContentValues.TAG, "saveBitmapImage: ", e)
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
                    Log.e(ContentValues.TAG, "saveBitmapImage: ", e)
                }
                values.put(MediaStore.Images.Media.DATA, imageFile.absolutePath)
                requireActivity().contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                Toast.makeText(requireContext(), "Saved...", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e(ContentValues.TAG, "saveBitmapImage: ", e)
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

}
