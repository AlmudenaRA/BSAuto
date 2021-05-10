package com.example.bsauto

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*




class MainActivity : AppCompatActivity() {

    enum class ProviderType{
        BASIC,
        GOOGLE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bundle = intent.extras
        val email = bundle?.getString("email")
        val provider = bundle?.getString("provider")



        //Guardar datos del login del usuario autenticado
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
        prefs.putString("email", email)
        prefs.putString("provider", provider)
        prefs.apply()
    }

    //Mostrar el email y contraseña
    private fun exitSession(){
        //botón para cerrar sesión
        //logOutButton.setOnClickListener{
            //Limpiar datos usuario cuando se cierra sesión
        //val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
        //prefs.clear() //Borrar datos asociados al email y provider
        //prefs.apply()
          //  FirebaseAuth.getInstance().signOut()
            //onBackPressed()//vuelve a la pantalla anterior
        //}
    }
}