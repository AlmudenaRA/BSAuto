package com.example.bsauto.exit

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.bsauto.LoginActivity
import com.example.bsauto.R
import com.example.bsauto.SingUpActivity
import com.google.firebase.auth.FirebaseAuth

class ExitFragment : Fragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        exitSession()
    }


    /**
     * Función para ir a la actividad de registrarse
     */
    private fun showLogin(){
        val loginIntent = Intent(context, LoginActivity::class.java)
        startActivity(loginIntent)
    }

    private fun exitSession(){
        //Limpiar datos usuario cuando se cierra sesión
        val prefs: SharedPreferences.Editor = requireContext().getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
        prefs.clear() //Borrar datos asociados al email y provider
        prefs.apply()
        FirebaseAuth.getInstance().signOut()
        showLogin()//vuelve a la pantalla login

    }


}