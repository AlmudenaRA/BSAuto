package com.example.bsauto.listpost

import android.content.Intent.getIntent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.ViewPager
import com.example.bsauto.R
import com.example.bsauto.myposts.Post
import com.example.bsauto.util.RoundImagePicasso
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import kotlinx.android.synthetic.main.fragment_detail.*
import kotlinx.android.synthetic.main.fragment_my_posts.*
import java.io.ByteArrayOutputStream

class DetailFragment : Fragment() {

    private var post: Post? = null
    lateinit var storage: FirebaseStorage
    private val db = FirebaseFirestore.getInstance() //Instancia a la base de datos

    val user = Firebase.auth.currentUser
    private lateinit var auth: FirebaseAuth
    private var email: String? = user!!.email
    private lateinit var image: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = Firebase.auth
        storage = Firebase.storage

        loadData()
        viewData()
    }


//    //Imprimir por pantalla
    private fun viewData(){

        txt_detail_brand.text = post?.brand
        txt_detail_fuel.text = post?.fuel
        txt_detail_change.text = post?.change
        txt_detail_km.text = post?.km
        txt_detail_cv.text = post?.cv
        txt_detail_year.text = post?.year
        txt_detail_price.text = post?.price
        txt_description.text = post?.description
        txt_detail_nameUser.text = post?.name
        txt_detail_phone.text = post?.phone

        post?.image?.let {
                mCover ->
            Picasso.get()
                .load(mCover)
                .transform(RoundImagePicasso())
                .placeholder(R.drawable.ic_car_foreground)
                .into(img_detail)
        }

    }

    private fun loadData() {
        val baos = ByteArrayOutputStream()

        val data = baos.toByteArray()
        val imageRef = storage.reference.child("images/post/${auth.uid}.jpg")
        var uploadTask = imageRef.putBytes(data)
        //descarga y referencia URl
        uploadTask.addOnFailureListener {
            Log.i("firebase", "Error al descargar la foto de storage")
        }.addOnSuccessListener { taskSnapshot ->
            val dowuri = taskSnapshot.metadata!!.reference!!.downloadUrl
            dowuri.addOnSuccessListener { task ->

               // val uri = task.toString()

                db.collection("posts").document(email!!).get().addOnSuccessListener {

                    post?.brand = it.get("brand") as String
                    post?.fuel = it.get("fuel") as String
                    post?.change = it.get("change") as String
                    post?.km = it.get("km") as String
                    post?.cv = it.get("cv") as String
                    post?.year = it.get("year") as String
                    post?.price = it.get("price") as String
                    post?.description = it.get("description") as String
                    post?.province = it.get("province") as String
                    post?.city = it.get("city") as String
                    post?.name = it.get("name") as String
                    post?.phone = it.get("phone") as String
                    post?.image = it.get("image") as String

                }
            }
        }

    }


    companion object {

    }
}