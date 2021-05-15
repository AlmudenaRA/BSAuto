package com.example.bsauto.myposts

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.example.bsauto.LoginActivity
import com.example.bsauto.R
import com.example.bsauto.util.UtilImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_singup.*
import kotlinx.android.synthetic.main.fragment_my_posts.*
import kotlinx.android.synthetic.main.fragment_my_profile.*
import java.io.ByteArrayOutputStream
import java.io.IOException


class MyPostsFragment : Fragment() {

    val user = Firebase.auth.currentUser
    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance() //Instancia a la base de datos

    private lateinit var IMAGE_NAME: String
    private lateinit var FOTO: Bitmap
    private val GALLERY = 1
    private val IMAGE_DIR = "/BSAuto"
    private var IMAGE_URI: Uri? = null
    private val CAMERA = 2

    private var email: String = user.email

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_posts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = Firebase.auth

        setup()



    }

    private fun setup(){
        img_my_post.setOnClickListener(){
            initDialogPhoto()
        }

        //Recupera los datos del anuncio y los muestra en el txt
        db.collection("posts").document(email).get().addOnSuccessListener {
            txt_mypost_brand.setText(it.get("brand") as String?)
            txt_mypost_brand.setText(it.get("brand") as String?)
            txt_mypost_fuel.setText(it.get("fuel") as String?)
            txt_mypost_change.setText(it.get("change") as String?)
            txt_mypost_km.setText(it.get("km") as String?)
            txt_mypost_power.setText(it.get("cv") as String?)
            txt_mypost_year.setText(it.get("year") as String?)
            txt_mypost_price.setText(it.get("price") as String?)
            txtm_mypost_description.setText(it.get("description") as String?)
            txt_mypost_province.setText(it.get("province") as String?)
            txt_mypost_city.setText(it.get("city") as String?)
            txt_mypost_name.setText(it.get("name") as String?)
            txt_mypost_phone.setText(it.get("phone") as String?)
            //img_my_post.setImageURI(it.get("image") as String?)


        }

        //Publicar anuncio o modificarlo en caso de que ya exista
        btn_mypost_topost.setOnClickListener(){

            db.collection("posts").document(email).set(
                    hashMapOf("brand" to txt_mypost_brand.text.toString(),
                    "fuel" to txt_mypost_fuel.text.toString(),
                    "change" to txt_mypost_change.text.toString(),
                    "km" to txt_mypost_km.text.toString(),
                    "cv" to txt_mypost_power.text.toString(),
                    "year" to txt_mypost_year.text.toString(),
                    "price" to txt_mypost_price.text.toString(),
                    "description" to txtm_mypost_description.text.toString(),
                    "province" to txt_mypost_province.text.toString(),
                    "city" to txt_mypost_city.text.toString(),
                    "name" to txt_mypost_name.text.toString(),
                    "phone" to  txt_mypost_phone.text.toString(),
                    "image" to img_my_post.toString())
            )

            Toast.makeText(context, getText(R.string.my_post_correct), Toast.LENGTH_SHORT).show()

        }

        //Eliminar anuncio
        btn_mypost_delete.setOnClickListener(){
            db.collection("posts").document(email).delete()
            //Toast.makeText(context, getText(R.string.my_post_delete), Toast.LENGTH_SHORT).show()

            it.findNavController().navigate(R.id.nav_start)
        }
    }


    //Métodos para obtener las fotos del vehiculo
    //===================================================

    /**
     * Muestra el diálogo para tomar foto o elegir de la galería
     */
    private fun initDialogPhoto() {
        val fotoDialogoItems = arrayOf(
                "Select from gallery",
                "Take camera photo"
        )
        //Dialogo para eligir opciones
        context?.let {
            AlertDialog.Builder(it)
                    .setTitle(getString(R.string.singup_camera_title_option))
                    .setItems(fotoDialogoItems) { _, modo ->
                        when (modo) {
                            0 -> selectPhotoGallery()
                            1 -> takePhotoCamera()
                        }
                    }
                    .show()
        }
    }

    /**
     * Función para elegir una foto de la galeria
     */
    private fun selectPhotoGallery() {
        val galleryIntent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        startActivityForResult(galleryIntent, GALLERY)
    }

    /**
     * Función para tomar una foto con la cámara
     */
    private fun takePhotoCamera() {

        val builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // Nombre de la imagen
        IMAGE_NAME = UtilImage.createNameFile()
        // Salvamos el fichero
        val fichero = UtilImage.salvarImagen(IMAGE_DIR, IMAGE_NAME, requireContext())!!
        IMAGE_URI = Uri.fromFile(fichero)

        intent.putExtra(MediaStore.EXTRA_OUTPUT, IMAGE_URI)

        startActivityForResult(intent, CAMERA)
    }

    /**
     * Cuando ejecutamos una actividad y da un resultado
     * @param requestCode Int
     * @param resultCode Int
     * @param data Intent?
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //Si cancela no hace nada
        if (resultCode == AppCompatActivity.RESULT_CANCELED) {
            Log.d("singup", "Se ha cancelado")
        }
        //si elige la opcion de galeria entra en la galeria
        if (requestCode == GALLERY) {
            Log.d("singup", "Entramos en Galería")
            if (data != null) {
                // Obtenemos su URI
                val contentURI = data.data!!
                try {
                    FOTO = differentVersion(contentURI)
                    FOTO = UtilImage.scaleImage(FOTO, 800, 800)
                    img_my_post.setImageBitmap(FOTO)//mostramos la imagen
                    UtilImage.redondearFoto(img_my_post)
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(context, getText(R.string.error_gallery), Toast.LENGTH_SHORT).show()
                }
            }
        } else if (requestCode == CAMERA) {
            Log.d("singup", "Entramos en Camara")
            //cogemos la imagen
            try {
                FOTO = differentVersion(IMAGE_URI!!)
                FOTO = UtilImage.scaleImage(FOTO, 800, 800)
                // Mostramos la imagen
                img_my_post.setImageBitmap(FOTO)
                UtilImage.redondearFoto(img_my_post)
            } catch (e: NullPointerException) {
                e.printStackTrace()
            } catch (ex: Exception) {
                Toast.makeText(context, getText(R.string.error_camera), Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Metodo que devuleve un bitmap depende de la version
     * @return un bitmap
     */
    fun differentVersion(contentURI: Uri): Bitmap {
        //Para controlar la version de android usar uno u otro
        val bitmap: Bitmap
        bitmap = if (Build.VERSION.SDK_INT < 28) {
            MediaStore.Images.Media.getBitmap(context?.contentResolver, contentURI);
        } else {
            val source: ImageDecoder.Source = ImageDecoder.createSource(context?.contentResolver!!, contentURI)
            ImageDecoder.decodeBitmap(source)
        }
        return bitmap;
    }

//    private fun loadImage(string: String, user: FirebaseUser) {
//        if (!this::FOTO.isInitialized) {
//            return
//        }
//        val baos = ByteArrayOutputStream()
//        FOTO.compress(Bitmap.CompressFormat.JPEG, 40, baos)
//        val data = baos.toByteArray()
//        val imageRef = storage.reference.child("images/post/${auth.uid}.jpg")
//        var uploadTask = imageRef.putBytes(data)
//        //descarga y referencia URl
//        uploadTask.addOnFailureListener {
//            Log.i("firebase", "Error al subir la foto a storage")
//        }.addOnSuccessListener { taskSnapshot ->
//            val dowuri = taskSnapshot.metadata!!.reference!!.downloadUrl
//            dowuri.addOnSuccessListener { task ->
//                val profileUpdates = userProfileChangeRequest {
//                    photoUri = task
//                    Log.i("firebase", "uri: $task")
//                }
//                //modifica con los cambios de la uri
//                user.updateProfile(profileUpdates)
//                        .addOnCompleteListener { task ->
//                            if (task.isSuccessful) {
//                                if (user.photoUrl != null) {
////                                    Log.i("util", "Carga imagen")
////                                    Picasso.get()
////                                        .load(user.photoUrl)
////                                        .transform(RoundImagePicasso())
////                                        .into(MainActivity.img_user)
//                                }
//                                Log.d("TAG", "uri profile good")
//                            }
//                        }
//            }
//        }
//
//    }

    companion object {
        private const val TAG = ":::MYPOST"
    }

}