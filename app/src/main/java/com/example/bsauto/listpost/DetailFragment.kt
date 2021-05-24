package com.example.bsauto.listpost

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import kotlinx.android.synthetic.main.fragment_detail.*

class DetailFragment(post: Post?) : Fragment() {

    private var post: Post? = post


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewData()
    }


    /**
     * Muestra los datos del anuncio seleccionado
     */
    private fun viewData(){

        txt_detail_brand.setText(post?.brand)
        txt_detail_fuel.setText(post?.fuel)
        txt_detail_change.setText(post?.change)
        txt_detail_km.setText(post?.km)
        txt_detail_cv.setText(post?.cv)
        txt_detail_year.setText(post?.year)
        txt_detail_price.setText(post?.price)
        txt_description.setText(post?.description)

        post?.image?.let {
                mCover ->
            Picasso.get()
                .load(mCover)
                .transform(RoundImagePicasso())
                .placeholder(R.drawable.ic_car_foreground)
                .into(img_detail)
        }

    }

    companion object {

    }
}