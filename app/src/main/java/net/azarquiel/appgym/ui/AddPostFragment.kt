import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
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
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import net.azarquiel.appgym.R
import net.azarquiel.appgym.databinding.FragmentAddPostBinding
import net.azarquiel.appgym.databinding.FragmentChatBinding
import net.azarquiel.appgym.model.Comentario
import net.azarquiel.appgym.model.Comida
import net.azarquiel.appgym.model.Post
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddPostFragment : DialogFragment() {

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
    private lateinit var coment: String
    private lateinit var foto: Drawable
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

        binding.btnpublicaraddpost.setOnClickListener {
            foto = binding.ivapfotopost.drawable
            coment = binding.edappiecoment.text.toString()
            email = datosUserSH.getString("email", "")
            val sdf2 = SimpleDateFormat("ddMMyyyy", Locale.getDefault())
            formattedDate2 = sdf2.format(currentDate)
            post = Post(foto.toString(), mutableListOf(), mutableListOf(), coment, email.toString(), formattedDate," " )


            newDocument["Likes"] = post.Likes
            newDocument["Comentarios"] = post.Comentarios
            newDocument["PieComent"] = post.PieComent
            newDocument["Usuario"] = post.Usuario
            newDocument["Fecha"] = post.Fecha


            publicarPost()
            publicarPostTotales()
            dismiss()
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
                        posts.document(postid).set(newDocument)
                    }else{
                        val postid = "post01"+"_${email}"
                        newDocument["id"] = postid
                        posts.document(postid).set(newDocument)
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
                        postsTotales.document(postid).set(newDocument)
                    }else{
                        val postid = "post01"+"_${email}"
                        newDocument["id"] = postid
                        postsTotales.document(postid).set(newDocument)
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
    private fun uploadImage(imageUri: Uri) {
        val sdf = SimpleDateFormat("yyyy_M_dd_HH_mm_ss", Locale.getDefault())
        val now = Date()
        val filename = sdf.format(now)
        val storageReference = FirebaseStorage.getInstance().getReference("FotosUsers/$filename ${email}")
        storageReference.putFile(imageUri)
            .addOnSuccessListener { taskSnapshot ->
                storageReference.downloadUrl.addOnSuccessListener { uri ->
                    newDocument["Foto"] = uri.toString()
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
        }
    }

    private fun msg(s: String) {

    }

    private fun getImageUri(inContext: Context, inImage: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(inContext.contentResolver, inImage, "Title", null)
        return Uri.parse(path)
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
