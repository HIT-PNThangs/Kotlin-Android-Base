package com.example.pnt.hit.retrofit.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.pnt.hit.retrofit.R
import com.example.pnt.hit.retrofit.model.Photo
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_photo.view.*

class PhotoAdapter(
    val photoList: List<Photo>
) : RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder>() {

    private var onClickItem: ((position: Int) -> Unit)? = null
    fun setOnClickItem(event: ((position: Int) -> Unit)) {
        onClickItem = event
    }

    inner class PhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindData(photo: Photo) {
            itemView.tvId.text = "${photo.id}"
            itemView.tvTitle.text = photo.title
            Picasso.get().load(photo.url).into(itemView.image)
        }

        init {
            itemView.setOnClickListener {
                onClickItem?.invoke(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_photo, parent, false)
        return PhotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        holder.bindData(photoList[position])
    }

    override fun getItemCount() = photoList.size
}