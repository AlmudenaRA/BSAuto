package com.example.bsauto

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_login.txt_email
import kotlinx.android.synthetic.main.activity_login.txt_pass
import kotlinx.android.synthetic.main.activity_singup.*

class SingUpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_singup)

        //setup()
    }

    private fun setup(){
        login_singup.setOnClickListener(){
            //Comprueba si el email y password no están vacíos o nulos
            if (txt_email_singup.text.isNullOrEmpty() && txt_pass_singup.text.isNullOrEmpty()){
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(txt_email_singup.text.toString(),
                    txt_pass_singup.text.toString()).addOnCompleteListener{ //Notifica que la acción se ha completado

                    if (it.isSuccessful){ //Si se completa correctamente vuelve a la ventana login
                        showSignUp(it.result?.user?.email ?:"",ProviderType.BASIC)
                    }else{
                        showAlert()
                    }
                }
            }
        }
    }

    //Función para notificar una alerta de error
    private fun showAlert(){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage("Se ha producido un error autenticando al usuario")
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    //Función para volver a la actividad del login
    private fun showSignUp(email: String, provider: ProviderType){
        val loginIntent = Intent(this, LoginActivity::class.java).apply {
            putExtra("email", email)
            putExtra("provider", provider.name)
        }
        startActivity(loginIntent)

    }
}