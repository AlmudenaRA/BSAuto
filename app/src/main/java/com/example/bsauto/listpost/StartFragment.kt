package com.example.bsauto.listpost

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bsauto.MainActivity
import com.example.bsauto.R
import com.example.bsauto.myposts.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_start.*
import java.util.concurrent.Executors

class StartFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: ListAdapter
    private var postList = mutableListOf<Post>()

    private val db = FirebaseFirestore.getInstance() //Instancia a la base de datos

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_start, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = Firebase.auth
        recyclerview_start.layoutManager = LinearLayoutManager(context)
        setup()
    }

    private fun setup(){
        postList.clear()
        adapter = ListAdapter(postList){
            eventPost(it)
        }
        recyclerview_start.adapter = adapter

        //Recibir datos en tiempo real
        db.collection("posts").get()
                .addOnSuccessListener { result ->
                    for (item in result) {
                        val post = item.toObject(Post::class.java)
                        postList.add(post)
                        cargaAdapter(postList)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.i(TAG, "error al cargar posts")

                }

    }

    private fun cargaAdapter(posts: MutableList<Post>) {
        val executor = Executors.newSingleThreadExecutor() //Usa un solo hilo
        val handler = Handler(Looper.getMainLooper()) //Ejecuta un bucle de mensajes para un hilo
        executor.execute {

            handler.post {
                adapter = ListAdapter(posts) {
                    eventPost(it)
                }

                recyclerview_start.adapter = adapter
                // Avismos que ha cambiado
                adapter.notifyDataSetChanged()
                recyclerview_start.setHasFixedSize(true)

            }
        }
    }

    /**
     * Función cuando pulsas un anuncio
     */
    private fun eventPost(post: Post){
        if ((activity as MainActivity?)!!.isEventoFila) {
            openDetailPost(post)
        }
    }

    /**
     * Abre un anuncio como Fragment
     * @param post Post
     */
    private fun openDetailPost(post: Post) {
        Log.i(TAG, "Abrir detalle anuncio")
        val addDetail = DetailFragment(post)
        val transaction = requireActivity()!!.supportFragmentManager.beginTransaction()
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        transaction.add(R.id.nav_host_fragment, addDetail) //agrega el fragment
        transaction.addToBackStack(null)
        transaction.commit()
    }


    companion object {
        private const val TAG = ":::STARTFRAGMENT"
    }


}