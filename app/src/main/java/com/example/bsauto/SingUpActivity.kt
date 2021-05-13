package com.example.bsauto

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.StrictMode
import android.provider.MediaStore
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import android.app.AlertDialog
import com.example.bsauto.util.UtilImage
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_singup.*
import java.io.ByteArrayOutputStream
import java.io.IOException

class SingUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private lateinit var pass: String
    private lateinit var email: String

    lateinit var storage: FirebaseStorage

    private lateinit var IMAGE_NAME: String
    private lateinit var FOTO: Bitmap
    private val GALLERY = 1
    private val IMAGE_DIR = "/BSAuto"
    private var IMAGE_URI: Uri? = null
    private val CAMERA = 2


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_singup)

        auth = Firebase.auth
        storage = Firebase.storage
        setup()
    }

    /**
     * Función que muestra un error si los campos están vacíos
     */
    private fun isEmpty(txt: TextView, txtLayout: TextInputLayout, context: Context){

        if(txt.text.isEmpty()) {
            txtLayout.error = context.resources.getString(R.string.error_empty)
        }

    }

    /**
     * Función para limpiar los errores
     */
    private fun clearErrors(vararg error: TextInputLayout){
        for(e in error) e.error = null
    }


    /**
     * Función para crear a un usuario a través de su email y contraseña
     */
    private fun setup(){
        pass = txt_pass_singup.text.toString()
        email = txt_email_singup.text.toString()

        btn_singup.setOnClickListener(){
            //Limpiar errores
            clearErrors(txt_layout_email_singup, txt_layout_pass_singup, txt_layout_user)

            //Comprueba si el email, password y nombre de usuario no están vacíos o nulos
            if (!txt_email_singup.text.isNullOrEmpty() && !txt_pass_singup.text.isNullOrEmpty() && !txt_user.text.isNullOrEmpty()) {
                //Crea el usuario con el email y contraseña
                auth.createUserWithEmailAndPassword(txt_email_singup.text.toString(), txt_pass_singup.text.toString())
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                Log.d(Companion.TAG, "createUserWithEmail:success")
                                Toast.makeText(baseContext, "Authentication correct.",
                                        Toast.LENGTH_SHORT).show()
                                val user = auth.currentUser
                                updateUI(user)
                                showSignUp(email, pass)
                            } else {
                                Log.w(Companion.TAG, "createUserWithEmail:failure", task.exception)
                                Toast.makeText(baseContext, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show()

                                txt_layout_pass_singup.error = resources.getString(R.string.error_singup)
                            }
                        }
            }else{
                isEmpty(txt_email_singup, txt_layout_email_singup,this)
                isEmpty(txt_pass_singup, txt_layout_pass_singup,this)
                isEmpty(txt_user, txt_layout_user,this)
            }

        }

        imgbtn_user.setOnClickListener(){
            initDialogPhoto()
        }

        txt_singin_singup.setOnClickListener(){
            val loginIntent = Intent(this, LoginActivity::class.java)
            startActivity(loginIntent)
        }
    }

    /**
     * Función que modifica al usuario
     */
    private fun updateUI(user: FirebaseUser) {
        val profileUpdates = userProfileChangeRequest {
            displayName = txt_user.text.toString()
            loadImage(displayName!!, user)
        }

        user!!.updateProfile(profileUpdates)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "User profile updated.")
                    }
                }
    }

    /**
     * Función para volver a la actividad del login
     */
    private fun showSignUp(email: String, pass: String){
        val loginIntent = Intent(this, LoginActivity::class.java).apply {
            putExtra("email", email)
            putExtra("pass", pass)
        }
        startActivity(loginIntent)

    }

    //Métodos para obtener la foto de perfil del usuario
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
        AlertDialog.Builder(this)
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
        val fichero = UtilImage.salvarImagen(IMAGE_DIR, IMAGE_NAME, applicationContext)!!
        IMAGE_URI = Uri.fromFile(fichero)

        intent.putExtra(MediaStore.EXTRA_OUTPUT, IMAGE_URI)

        startActivityForResult(intent, CAMERA)
    }

    private fun loadImage(string: String, user: FirebaseUser) {
        if (!this::FOTO.isInitialized) {
            return
        }
        val baos = ByteArrayOutputStream()
        FOTO.compress(Bitmap.CompressFormat.JPEG, 40, baos)
        val data = baos.toByteArray()
        val imageRef = storage.reference.child("images/users/${auth.uid}.jpg")
        var uploadTask = imageRef.putBytes(data)
        //descarga y referencia URl
        uploadTask.addOnFailureListener {
            Log.i("firebase", "Error al subir la foto a storage")
        }.addOnSuccessListener { taskSnapshot ->
            val dowuri = taskSnapshot.metadata!!.reference!!.downloadUrl
            dowuri.addOnSuccessListener { task ->
                val profileUpdates = userProfileChangeRequest {
                    photoUri = task
                    Log.i("firebase", "uri: $task")
                }
                //modifica con los cambios de la uri
                user.updateProfile(profileUpdates)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("TAG", "uri profile good")
                        }
                    }
            }
        }

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
        if (resultCode == RESULT_CANCELED) {
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
                    imgbtn_user.setImageBitmap(FOTO)//mostramos la imagen
                    UtilImage.redondearFoto(imgbtn_user)
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(this, getText(R.string.error_gallery), Toast.LENGTH_SHORT).show()
                }
            }
        } else if (requestCode == CAMERA) {
            Log.d("singup", "Entramos en Camara")
            //cogemos la imagen
            try {
                FOTO = differentVersion(IMAGE_URI!!)
                FOTO = UtilImage.scaleImage(FOTO, 800, 800)
                // Mostramos la imagen
                imgbtn_user.setImageBitmap(FOTO)
                UtilImage.redondearFoto(imgbtn_user)
            } catch (e: NullPointerException) {
                e.printStackTrace()
            } catch (ex: Exception) {
                Toast.makeText(this, getText(R.string.error_camera), Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Metodo que devueleve un bitmap dependiendo de la version
     * @return un bitmap
     */
    fun differentVersion(contentURI: Uri): Bitmap {
        //Para controlar la version de android usar uno u otro
        val bitmap: Bitmap
        bitmap = if (Build.VERSION.SDK_INT < 28) {
            MediaStore.Images.Media.getBitmap(contentResolver, contentURI);
        } else {
            val source: ImageDecoder.Source = ImageDecoder.createSource(contentResolver, contentURI)
            ImageDecoder.decodeBitmap(source)
        }
        return bitmap;
    }

    companion object {
        internal const val TAG = ":::SIGNUP"
    }
}