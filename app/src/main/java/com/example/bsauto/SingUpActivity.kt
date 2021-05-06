package com.example.bsauto

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_singup.*

class SingUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private lateinit var pass: String
    private lateinit var email: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_singup)

        auth = Firebase.auth
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
     * Función para crear a un usuario
     */
    private fun setup(){
        pass = txt_pass_singup.text.toString()
        email = txt_email_singup.text.toString()

        btn_singup.setOnClickListener(){
            //Limpiar errores
            clearErrors(txt_layout_email_singup, txt_layout_pass_singup)

            //Comprueba si el email, password y nombre de usuario no están vacíos o nulos
            if (!txt_email_singup.text.isNullOrEmpty() && !txt_pass_singup.text.isNullOrEmpty()
                    && !txt_user.text.isNullOrEmpty()) {
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
                                updateUI(null)
                                txt_layout_email_singup.error = resources.getString(R.string.error_email)
                            }
                        }
            }else{
                isEmpty(txt_email_singup, txt_layout_email_singup,this)
                isEmpty(txt_pass_singup, txt_layout_pass_singup,this)
                isEmpty(txt_user, txt_layout_user,this)
            }

        }
    }

    /**
     * Función que modifica al usuario
     */
    private fun updateUI(user: FirebaseUser?) {
        val profileUpdates = userProfileChangeRequest {
            displayName = txt_user.text.toString()
            photoUri = Uri.parse("https://example.com/jane-q-user/profile.jpg")
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

    companion object {
        private const val TAG = ":::SIGNUP"
    }
}