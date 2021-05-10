package com.example.bsauto

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_login.txt_email
import kotlinx.android.synthetic.main.activity_login.txt_pass
import kotlinx.android.synthetic.main.activity_singup.*

class LoginActivity : AppCompatActivity() {

    enum class ProviderType{
        BASIC,
        GOOGLE
    }

    private val GOOGLE_SING_IN = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val bundle = intent.extras
        val email = bundle?.getString("email")
        val pass = bundle?.getString("pass")

        setup()
        session()

    }

    override fun onStart() {
        super.onStart()
        linearLayoutLogin.visibility = View.VISIBLE
    }

    /**
     * Función que comprueba si hay una sesión activa
     */
    private fun session(){
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        val email = prefs.getString("email", null)
        val provider = prefs.getString("provider", null)

        if(email != null && provider != null){
            linearLayoutLogin.visibility = View.INVISIBLE
            showMain(ProviderType.valueOf(provider))
        }
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

    private fun setup(){

        //Acción al hacer click sobre el botón login
        btn_login.setOnClickListener(){
            //Limpiar errores
            clearErrors(txt_layout_email, txt_layout_pass, txt_layout_user)

            //Comprueba si el email y password no están vacíos o nulos
            if (!txt_email.text.isNullOrEmpty() && !txt_pass.text.isNullOrEmpty()){
                FirebaseAuth.getInstance().signInWithEmailAndPassword(txt_email.text.toString(),
                    txt_pass.text.toString()).addOnCompleteListener{ //Notifica que la acción se ha completado

                        if (it.isSuccessful){
                            showMain(ProviderType.BASIC)
                        }else{
                            txt_layout_pass.error = resources.getString(R.string.error_login)
                        }
                }
            }else{
                isEmpty(txt_email, txt_layout_email,this)
                isEmpty(txt_pass, txt_layout_pass,this)
            }

        }

        //Acción al hacer click sobre el botón singup
        login_singup.setOnClickListener(){
            showSignUp()
        }

        //Acción al hacer click sobre el botón de google
        btn_google.setOnClickListener{
            val googleConf = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build()

            val googleClient = GoogleSignIn.getClient(this, googleConf)
            googleClient.signOut()
            startActivityForResult(googleClient.signInIntent, GOOGLE_SING_IN)
        }

    }

    /**
     * Función para notificar una alerta de error
     */
    private fun showAlert(){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage("Se ha producido un error autenticando al usuario")
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    /**
     * Función para ir a la actividad principal
     */
    private fun showMain(provider: ProviderType){
        val mainIntent = Intent(this, MainActivity::class.java).apply {
            putExtra("provider", provider.name)
        }
        startActivity(mainIntent)
        finish()
    }

    /**
     * Función para ir a la actividad de registrarse
     */
    private fun showSignUp(){
        val singUpIntent = Intent(this, SingUpActivity::class.java)
        startActivity(singUpIntent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == GOOGLE_SING_IN){
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try{
                val account:GoogleSignInAccount? = task.getResult(ApiException::class.java)

                if(account != null){
                    val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                    FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener(){
                        if (it.isSuccessful){
                            showMain(ProviderType.GOOGLE)
                        }else{
                            showAlert()
                        }
                    }

                }
            }catch (e: ApiException){
                showAlert()
            }
        }
    }

}