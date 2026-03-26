package com.example.contactformapp

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import de.hdodenhof.circleimageview.CircleImageView

class ContactAdapter(
    private var contacts: MutableList<Contact>,
    private val onEdit: (Contact) -> Unit,
    private val onDelete: (Contact) -> Unit
) : RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {

    inner class ContactViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivProfile: CircleImageView = view.findViewById(R.id.ivProfileImage)
        val tvName: TextView           = view.findViewById(R.id.tvName)
        val tvPhone: TextView          = view.findViewById(R.id.tvPhone)
        val btnEdit: ImageButton       = view.findViewById(R.id.btnEdit)
        val btnDelete: ImageButton     = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_contact, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = contacts[position]
        holder.tvName.text  = contact.name
        holder.tvPhone.text = contact.phone

        if (contact.profileImageUri.isNotEmpty()) {
            Glide.with(holder.ivProfile.context)
                .load(Uri.parse(contact.profileImageUri))
                .circleCrop()
                .placeholder(R.drawable.ic_person_placeholder)
                .error(R.drawable.ic_person_placeholder)
                .into(holder.ivProfile)
        } else {
            holder.ivProfile.setImageResource(R.drawable.ic_person_placeholder)
        }

        holder.btnEdit.setOnClickListener   { onEdit(contact) }
        holder.btnDelete.setOnClickListener { onDelete(contact) }
    }

    override fun getItemCount(): Int = contacts.size

    fun updateList(newList: MutableList<Contact>) {
        contacts = newList
        notifyDataSetChanged()
    }
}