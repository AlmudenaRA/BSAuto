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
                    Log.i("fairebase", "error al cargar posts")

                }

    }

    private fun cargaAdapter(posts: MutableList<Post>) {
        val executor = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())
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
        this.findNavController().navigate(R.id.nav_detail)
    }

    companion object {
        private const val TAG = ":::STARFRAGMENT"
    }


}