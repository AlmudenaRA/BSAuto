package com.example.bsauto.listpost

import android.graphics.Bitmap
import android.graphics.BitmapFactory
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

        holder.postBrand.text = postList[position].brand
        holder.postPrice.text = postList[position].price
        holder.postCity.text = postList[position].city
        holder.postProvince.text = postList[position].province
        holder.postYear.text = postList[position].year
        holder.postFuel.text = postList[position].fuel
        holder.postChange.text = postList[position].change

        val docRef = FireStore.collection("posts")
        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    Picasso.get()
                        .load(postList[position].image)
                        .placeholder(R.drawable.ic_add_photo_foreground)
                        .into(holder.imgPostVehicle)

                    holder.imgPostVehicle.setImageBitmap(BitmapFactory.decodeResource(holder.imgPostVehicle?.resources,
                        R.drawable.ic_add_photo_foreground))
                } else {
                    Log.i(TAG, "Error: No exite fotografía")

                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "ERROR: " + exception.localizedMessage)

            }


        holder.imgPostVehicle.setOnClickListener{
            listener(postList[position])
        }

    }

    //Devuelve el tamaño de la lista
    override fun getItemCount(): Int {
        return postList.size
    }

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