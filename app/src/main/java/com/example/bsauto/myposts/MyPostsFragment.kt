package com.example.bsauto.myposts

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
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
import com.bumptech.glide.Glide
import com.example.bsauto.LoginActivity
import com.example.bsauto.R
import com.example.bsauto.util.RoundImagePicasso
import com.example.bsauto.util.UtilImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.google.firebase.storage.ktx.storageMetadata
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import kotlinx.android.synthetic.main.activity_singup.*
import kotlinx.android.synthetic.main.fragment_my_posts.*
import kotlinx.android.synthetic.main.fragment_my_profile.*
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.URI


@Suppress("DEPRECATION")
class MyPostsFragment: Fragment() {

    val user = Firebase.auth.currentUser
    private lateinit var auth: FirebaseAuth
    lateinit var storage: FirebaseStorage

    private var db : FirebaseFirestore = FirebaseFirestore.getInstance() //Instancia a la base de datos

    private lateinit var IMAGE_NAME: String
    private lateinit var FOTO: Bitmap
    private val GALLERY = 1
    private val IMAGE_DIR = "/BSAuto"
    private var IMAGE_URI: Uri? = null
    private val CAMERA = 2

    private var email: String? = user!!.email

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_posts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = Firebase.auth
        storage = Firebase.storage

        loadData()
        setup()
    }

    private fun setup(){
        img_my_post.setOnClickListener(){
            initDialogPhoto()
        }


        //Publicar anuncio o modificarlo en caso de que ya exista
        btn_mypost_topost.setOnClickListener(){

            //Obtiene la imagen que se encuentra en el ImageView
            img_my_post.buildDrawingCache()
            FOTO = img_my_post.getDrawingCache()

            changeData(FOTO)

            Toast.makeText(context, getText(R.string.my_post_correct), Toast.LENGTH_SHORT).show()

        }

        //Eliminar anuncio
        btn_mypost_delete.setOnClickListener(){
            db.collection("posts").document(email!!).delete()

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
        AlertDialog.Builder(context)
                .setTitle(getString(R.string.singup_camera_title_option))
                .setItems(fotoDialogoItems) { _, modo ->
                    when (modo) {
                        0 -> selectPhotoGallery()
                        1 -> takePhotoCamera()
                    }
                }
                .show()

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
            Log.d(TAG, "Se ha cancelado")
        }
        //si elige la opcion de galeria entra en la galeria
        if (requestCode == GALLERY) {
            Log.d(TAG, "Entramos en Galería")
            if (data != null) {
                // Obtenemos su URI
                val contentURI = data.data!!
                try {
                    FOTO = differentVersion(contentURI)
                    FOTO = UtilImage.scaleImage(FOTO, 800, 800)
                    img_my_post.setImageBitmap(FOTO)//Muestra la imagen
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
                img_my_post.setImageBitmap(FOTO)//Muestra la imagen
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

    private fun changeData(FOTO: Bitmap) {

        val storageRef = storage.reference
        val imageRef = storageRef.child("images/post/${auth.uid}.jpg")
        val baos = ByteArrayOutputStream()
        FOTO.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()
        var uploadTask = imageRef.putBytes(data)

        uploadTask.addOnFailureListener{
            Log.i(TAG, "Error al subir la foto a storage")
        }.addOnSuccessListener { taskSnapshot ->
            val dowuri = taskSnapshot.metadata!!.reference!!.downloadUrl
            dowuri.addOnSuccessListener { task ->

                val uri = task.toString()

                val post = hashMapOf("brand" to txt_mypost_brand.text.toString(),
                        "fuel" to txt_mypost_fuel.text.toString(),
                        "change" to txt_mypost_change.text.toString(),
                        "km" to txt_mypost_km.text.toString(),
                        "cv" to txt_mypost_power.text.toString(),
                        "year" to txt_mypost_year.text.toString(),
                        "price" to txt_mypost_price.text.toString(),
                        "description" to txtm_mypost_description.text.toString(),
                        "name" to txt_mypost_name.text.toString(),
                        "phone" to txt_mypost_phone.text.toString(),
                        "image" to uri)

                db.collection("posts").document(email!!).set(post)
                        .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully written!") }
                        .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }
            }
        }

    }



    private fun loadData() {

        db.collection("posts").document(email!!).get().addOnSuccessListener {
            txt_mypost_brand.setText(it.get("brand") as String?)
            txt_mypost_fuel.setText(it.get("fuel") as String?)
            txt_mypost_change.setText(it.get("change") as String?)
            txt_mypost_km.setText(it.get("km") as String?)
            txt_mypost_power.setText(it.get("cv") as String?)
            txt_mypost_year.setText(it.get("year") as String?)
            txt_mypost_price.setText(it.get("price") as String?)
            txtm_mypost_description.setText(it.get("description") as String?)
            txt_mypost_name.setText(it.get("name") as String?)
            txt_mypost_phone.setText(it.get("phone") as String?)

            Picasso.get()
                .load(it.get("image") as String?)
                .transform(RoundImagePicasso())
                .placeholder(R.drawable.ic_car_foreground)
                .into(img_my_post)

        }

    }

    companion object {
        private const val TAG = ":::MYPOST"
    }

}

