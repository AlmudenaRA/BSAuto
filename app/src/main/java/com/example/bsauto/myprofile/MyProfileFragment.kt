package com.example.bsauto.myprofile

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
import android.widget.Toast
import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.bsauto.LoginActivity
import com.example.bsauto.MainActivity
import com.example.bsauto.R
import com.example.bsauto.SingUpActivity
import com.example.bsauto.util.RoundImagePicasso
import com.example.bsauto.util.UtilImage
import com.google.android.material.navigation.NavigationView
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_my_profile.*
import java.io.ByteArrayOutputStream
import java.io.IOException

class MyProfileFragment : Fragment() {

    val user = Firebase.auth.currentUser
    private lateinit var auth: FirebaseAuth

    lateinit var storage: FirebaseStorage

    private lateinit var IMAGE_NAME: String
    private lateinit var FOTO: Bitmap
    private val GALLERY = 1
    private val IMAGE_DIR = "/BSAuto"
    private var IMAGE_URI: Uri? = null
    private val CAMERA = 2

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = Firebase.auth
        storage = Firebase.storage

        getData()
        setup()
    }

    /**
     * Función que recoge los datos del usuario y los muestra
     */
    private fun getData(){
        user?.let {
            val name = user.displayName
            val email = user.email
            val photo = user.photoUrl

            if(photo != null){
                Picasso.get()
                    .load(photo)
                    .transform(RoundImagePicasso())
                    .into(img_user_myprofile)
            }

            txt_user_my_profile.setText(name)
            txt_email_my_profile.setText(email)

        }

    }

    /**
     * Función para limpiar los errores
     */
    private fun clearErrors(vararg error: TextInputLayout){
        for(e in error) e.error = null
    }

    private fun setup(){
        //Acción al pulsar en el botón de modificar
        btn_update_my_profile.setOnClickListener(){
//            android.app.AlertDialog.Builder(context)
//                .setTitle(getText(R.string.caution))
//                .setMessage(getText(R.string.sure_update))
//                .setPositiveButton(getString(R.string.ok)) { _, _ ->
//                    Log.i("update", "usuario cambia")
                    updateUser(user)
//                }
//                .setNegativeButton(getString(R.string.cancel), null)
//                .show()
        }

        //Acción al pulsar en el botón eliminar
        btn_delete_my_profile.setOnClickListener(){
//            android.app.AlertDialog.Builder(context)
//                .setTitle(getText(R.string.caution))
//                .setMessage(getText(R.string.sure_delete))
//                .setPositiveButton(getString(R.string.ok)) { _, _ ->
                    user.delete()
                    //Cuando el usuario se elimina vuelve al login
                    val loginIntent = Intent(context, LoginActivity::class.java)
                    startActivity(loginIntent)
//                        .addOnCompleteListener { task ->
//                            if (task.isSuccessful) {
//                                Log.d(TAG, "User account deleted.")
//                            }
//                        }
//                }
//                .setNegativeButton(getString(R.string.cancel), null)
//                .show()

        }

        //Acción al pulsar la imagen
        img_user_myprofile.setOnClickListener(){
            initDialogPhoto()
        }
    }


    /**
     * Función que modifica al usuario
     */
    private fun updateUser(user: FirebaseUser) {
        val pass = txt_pass_my_profile.text.toString()
        val email = txt_email_my_profile.text.toString()

        val profileUpdates = userProfileChangeRequest {
            displayName = txt_user_my_profile.text.toString()
            loadImage(displayName!!, user)
        }

        user!!.updateProfile(profileUpdates)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        userDataNavigation()
                        Log.d(SingUpActivity.TAG, "User profile updated.")
                    }
                }

        user!!.updateEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "User email address updated.")
                }
            }

        if(pass.isNotEmpty()){
            user!!.updatePassword(pass)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d(TAG, "User password updated.")
                        }
                    }
        }


        Toast.makeText(context, getText(R.string.my_profile_upd_correct), Toast.LENGTH_SHORT).show()

    }

     fun userDataNavigation(){
        val user = Firebase.auth.currentUser
        user?.let {
            val name = user.displayName
            val email = user.email
            val photo = user.photoUrl

            if(photo != null){
                Picasso.get()
                        .load(photo)
                        .transform(RoundImagePicasso())
                        .into(MainActivity.img_user)
            }
            MainActivity.txt_header_user.text = name
            MainActivity.txt_header_email.text = email

        }
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
                                if (user.photoUrl != null) {
//                                    Log.i("util", "Carga imagen")
//                                    Picasso.get()
//                                        .load(user.photoUrl)
//                                        .transform(RoundImagePicasso())
//                                        .into(MainActivity.img_user)
                                }
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
                    img_user_myprofile.setImageBitmap(FOTO)//mostramos la imagen
                    UtilImage.redondearFoto(img_user_myprofile)
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
                img_user_myprofile.setImageBitmap(FOTO)
                UtilImage.redondearFoto(img_user_myprofile)
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


    companion object {
        private const val TAG = ":::MYPROFILE"
    }
}