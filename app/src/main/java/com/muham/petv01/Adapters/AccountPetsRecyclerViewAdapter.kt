package com.muham.petv01.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.muham.petv01.Accounts.Pets
import com.muham.petv01.R

class AccountPetsRecyclerViewAdapter(
    private val context: Context,
    private val petList: List<Pets>
) : RecyclerView.Adapter<AccountPetsRecyclerViewAdapter.PetsViewHolder>() {

    inner class PetsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val petNameTextView: TextView = itemView.findViewById(R.id.petNameTextView)
        val petAgeTextView: TextView = itemView.findViewById(R.id.petAgeTextView)
        val deletePetTextView: TextView = itemView.findViewById(R.id.deletePetTextView)
        val cellPetTypeImageView: ImageView = itemView.findViewById(R.id.cellPetTypeImageView)
        // Add other necessary view elements here
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PetsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.accountpet_cell, parent, false)

        return PetsViewHolder(view)
    }

    override fun getItemCount(): Int {
        return petList.size
    }

    override fun onBindViewHolder(holder: PetsViewHolder, position: Int) {
        val currentPets = petList[position]

        // Assuming Comment class has properties like petName and petAge
        holder.petNameTextView.text = currentPets.name
        holder.petAgeTextView.text = currentPets.age.toString()

        holder.deletePetTextView.setOnClickListener {
            showDeleteConfirmationDialog(position)
        }

        setPetTypeImage(holder.cellPetTypeImageView, currentPets.type)


    }
    private fun setPetTypeImage(imageView: ImageView, petType: String) {
        when (petType) {
            "Bird" -> imageView.setImageResource(R.drawable.bird)
            "Dog" -> imageView.setImageResource(R.drawable.dog)
            "Cat" -> imageView.setImageResource(R.drawable.cat)

            else -> imageView.setImageResource(R.drawable.dog)
        }
    }

    private fun showDeleteConfirmationDialog(position: Int) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Confirm Delete")
        builder.setMessage("Are you sure you want to delete this pet?")

        builder.setPositiveButton("Yes") { _, _ ->
            deletePet(position)
        }

        builder.setNegativeButton("No") { _, _ ->
            // Do nothing, user canceled deletion
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun deletePet(position: Int) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            val petId = petList[position].id // Assuming there is an "id" property in Pets class
            val userReference = FirebaseFirestore.getInstance().collection("Persons").document(userId)

            // Remove pet from "pets" array
            userReference.update("pets", FieldValue.arrayRemove(petList[position]))
                .addOnSuccessListener {
                    // Successfully removed pet from user's pets array

                    // Now, delete the pet document from "Persons" collection
                    FirebaseFirestore.getInstance().collection("Persons").document(userId)
                        .collection("pets").document(petId)
                        .delete()
                        .addOnSuccessListener {
                            // Successfully deleted the pet document
                        }
                        .addOnFailureListener { exception ->
                            // Handle error
                        }
                }
                .addOnFailureListener { exception ->
                    // Handle error
                }
        }
    }

}

