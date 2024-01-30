package com.muham.petv01.Fragments.AccountFragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.muham.petv01.Accounts.Persons
import com.muham.petv01.Accounts.Pets
import com.muham.petv01.Adapters.AccountPetsRecyclerViewAdapter
import com.muham.petv01.R

class AccountPetsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AccountPetsRecyclerViewAdapter
    private val db = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var accountPetsSwipeRefreshLayout: SwipeRefreshLayout


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_account_pets, container, false)

        recyclerView = view.findViewById(R.id.petsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        accountPetsSwipeRefreshLayout = view.findViewById(R.id.accountPetsSwipeRefreshLayout)

        accountPetsSwipeRefreshLayout.setOnRefreshListener {
            fetchPetsFromFirebase()

            accountPetsSwipeRefreshLayout.isRefreshing = false
        }

        fetchPetsFromFirebase()
        return view
    }

    private fun fetchPetsFromFirebase() {
        val userId = auth.currentUser?.uid

        if (userId != null) {
            val userReference = Firebase.firestore.collection("Persons").document(userId)

            // Firestore'da değişiklik olduğunda tetiklenecek listener
            userReference.addSnapshotListener { userDocumentSnapshot, error ->
                if (error != null) {
                    Log.e("AccountPetsFragment", "Error getting pets", error)
                    return@addSnapshotListener
                }

                if (userDocumentSnapshot != null && userDocumentSnapshot.exists()) {
                    val person = userDocumentSnapshot.toObject(Persons::class.java)
                    val pets = person?.pets ?: emptyList()

                    // Yeni veri geldiğinde RecyclerView'e güncelleme
                    updateRecyclerView(pets)
                }
            }
        }
    }


    private fun updateRecyclerView(pets: List<Pets>) {
        val petsRecyclerView = view?.findViewById<RecyclerView>(R.id.petsRecyclerView)

        // Do not forget to pass context to the adapter
        val petsAdapter = AccountPetsRecyclerViewAdapter(requireContext(), pets)

        val petsLayoutManager = LinearLayoutManager(context)

        petsRecyclerView?.adapter = petsAdapter
        petsRecyclerView?.layoutManager = petsLayoutManager

        // Notify the adapter that the data has changed
        petsAdapter.notifyDataSetChanged()
    }


}

