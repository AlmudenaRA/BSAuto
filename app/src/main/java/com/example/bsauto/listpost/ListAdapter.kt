package com.example.bsauto.listpost

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.bsauto.R
import com.example.bsauto.myposts.Post
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import kotlinx.android.synthetic.main.item_start.view.*

class ListAdapter(
    private val postList : MutableList<Post>,
    private val listener: (Post) -> Unit) : RecyclerView.Adapter<ListAdapter.ListViewHolder>() {

    private var FireStore: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        return ListViewHolder( LayoutInflater.from(parent.context)
            .inflate(R.layout.item_start, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {

        var postImage: String = postList[position].image

        holder.postBrand.text = postList[position].brand
        holder.postPrice.text = postList[position].price
        holder.postCity.text = postList[position].city
        holder.postProvince.text = postList[position].province
        holder.postYear.text = postList[position].year
        holder.postFuel.text = postList[position].fuel
        holder.postChange.text = postList[position].change
        //imagenPost(postList[position], holder, position)

        val docRef = FireStore.collection("posts")
        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    Picasso.get()
                        .load(postList[position].image)
                        .placeholder(R.drawable.ic_add_photo_foreground)
                        .into(holder.imgPostVehicle)
                } else {
                    Log.i(TAG, "Error: No exite fotografía")

                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "ERROR: " + exception.localizedMessage)

            }

//        Picasso.get().load(postImage).into(object : Target {
//            override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
//                if (bitmap != null) {
//                    holder.imgPostVehicle.setImageBitmap(bitmap)
//                }
//            }
//
//            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
//            override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {}
//
//        })

        holder.imgPostVehicle.setOnClickListener{
            listener(postList[position])
        }

    }

    //Rellena y actualiza el listado de películas
    fun refreshList(moveList: ArrayList<Post>){
        this.postList.addAll(moveList)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return postList.size
    }

    /**
     * Devuelve la imagen
     */
//    private fun imagenPost(post: Post, holder: ListViewHolder, position: Int) {
//        // Buscamos la foto
//        val docRef = FireStore.collection("posts")
//        docRef.get()
//            .addOnSuccessListener { document ->
//                if (document != null) {
//                    Picasso.get()
//                        .load(postList[position].image)
//                        .placeholder(R.drawable.ic_add_photo_foreground)
//                        .into(holder.imgPostVehicle)
//                } else {
//                    Log.i(TAG, "Error: No exite fotografía")
//
//                }
//            }
//            .addOnFailureListener { exception ->
//                Log.d(TAG, "ERROR: " + exception.localizedMessage)
//
//            }
//    }

    /**
     * Holder que encapsula los objetos a mostrar en la lista
     */
    class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Elementos graficos con los que nos asociamos
        var imgPostVehicle = itemView.img_item_star
        var postBrand = itemView.txt_start_brand
        var postPrice = itemView.txt_price
        var postCity = itemView.txt_city
        var postProvince = itemView.txt_province
        var postYear = itemView.txt_year
        var postFuel = itemView.txt_fuel
        var postChange = itemView.txt_change

    }

    companion object {
        private const val TAG = ":::LISTADAPTER"
    }

}