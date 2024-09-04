package com.cakirhalil.fotopaylama.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cakirhalil.fotopaylama.databinding.RecyclerRowBinding
import com.cakirhalil.fotopaylama.model.Post
import com.squareup.picasso.Picasso

class PostAdapter(private val postList : ArrayList<Post>) : RecyclerView.Adapter<PostAdapter.PostHolder>() {

    class PostHolder(val binding: RecyclerRowBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostHolder {
        val binding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return PostHolder(binding)
    }

    override fun getItemCount(): Int {
        return postList.size
    }

    override fun onBindViewHolder(holder: PostHolder, position: Int) {
        holder.binding.RYCemailTXT.text = postList[position].email
        holder.binding.RYCcommetTXT.text = postList[position].comment
        Picasso.get().load(postList[position].downloadUrl).into(holder.binding.RYCimageView)
    }
}