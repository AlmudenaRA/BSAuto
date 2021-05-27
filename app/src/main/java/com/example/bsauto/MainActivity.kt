package com.example.bsauto

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.bsauto.util.RoundImagePicasso
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.fragment_my_profile.*

enum class ProviderType{
    BASIC,
    GOOGLE
}


class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration

    val user = Firebase.auth.currentUser
    private lateinit var auth: FirebaseAuth
    var isEventoFila = true

    companion object{
        lateinit var img_user: ImageView
        lateinit var txt_header_user: TextView
        lateinit var txt_header_email: TextView
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = Firebase.auth

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val bundle = intent.extras
        val email = bundle?.getString("email")
        val provider = bundle?.getString("provider")

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(setOf(
                R.id.nav_start, R.id.nav_my_post, R.id.nav_map,
            R.id.nav_profile, R.id.nav_exit), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)


        //Guardar datos del login del usuario autenticado
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
        prefs.putString("email", email)
        prefs.putString("provider", provider)
        prefs.apply()

        userData()
    }


    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    /**
     * Muestra los datos del usuario en el navigation
     */
    private fun userData(){

        val navigationView: NavigationView = findViewById(R.id.nav_view)
        val headerView: View = navigationView.getHeaderView(0)
        txt_header_user = headerView.findViewById(R.id.txt_header_user)
        txt_header_email = headerView.findViewById(R.id.txt_header_email)
        img_user = headerView.findViewById(R.id.img_user)
        //obtenemos el email de la sesion y obtenemos el usuario

        val user = Firebase.auth.currentUser
        user?.let {
            val name = user.displayName
            val email = user.email
            val photo = user.photoUrl

            if(photo != null){
                Picasso.get()
                        .load(photo)
                        .transform(RoundImagePicasso())
                        .into(img_user)
            }
            txt_header_user.setText(name)
            txt_header_email.setText(email)

        }
    }


    override fun onBackPressed() {
        isEventoFila = true
        super.onBackPressed()
    }
}