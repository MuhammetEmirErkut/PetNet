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
import com.muham.petv01.Adapters.ForumPostRecyclerViewAdapter
import com.muham.petv01.Inheritance.ItemForPost
import com.muham.petv01.R
import java.text.SimpleDateFormat
import java.util.Locale

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AccountSavedFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AccountSavedFragment : Fragment() {
    private lateinit var accountSavedRecyclerView: RecyclerView
    private lateinit var accountPostsRecyclerViewAdapter: ForumPostRecyclerViewAdapter
    private lateinit var postList: MutableList<ItemForPost>

    private val db = FirebaseFirestore.getInstance()
    private lateinit var auth: FirebaseAuth

    private lateinit var accountSavedSwipeRefreshLayout: SwipeRefreshLayout

    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        auth = FirebaseAuth.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_account_saved, container, false)

        postList = mutableListOf()

        accountSavedRecyclerView = view.findViewById(R.id.accountSavedRecyclerView)
        accountSavedSwipeRefreshLayout = view.findViewById(R.id.accountSavedSwipeRepresh)
        accountPostsRecyclerViewAdapter = ForumPostRecyclerViewAdapter(postList)
        accountSavedRecyclerView.adapter = accountPostsRecyclerViewAdapter
        accountSavedRecyclerView.layoutManager = LinearLayoutManager(activity)

        // Kullanıcının kendi UID'sini al
        val currentUserUid = auth.currentUser?.uid

        accountSavedSwipeRefreshLayout.setOnRefreshListener {
            refreshFromData(currentUserUid)
            accountSavedSwipeRefreshLayout.isRefreshing = false
        }

        // Kullanıcının UID'sini kullanarak kendi postlarını getir
        loadAccountSaved(currentUserUid)

        return view
    }

    private fun refreshFromData(currentUserUid: String?) {

        val addedDocumentIds = mutableListOf<String>()

        // Daha önce eklenmiş belgelerin kimliklerini al
        for (item in postList) {
            addedDocumentIds.add(item.documentId)
        }

        if (currentUserUid != null) {
            db.collection("forum")
                .whereArrayContains("saves", currentUserUid)
                .get()
                .addOnSuccessListener { documents ->

                    postList.clear()

                    for (document in documents) {
                        val userPhoto = document.getString("userPhoto") ?: ""
                        val title = document.getString("title") ?: ""
                        val content = document.getString("content") ?: ""
                        val userName = document.getString("username") ?: ""
                        val documentId = document.id
                        val timestamp = document.getTimestamp("timestamp")
                        val time = if (timestamp != null) {
                            val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                            sdf.format(timestamp.toDate())
                        } else {
                            ""
                        }
                        val likesList = document.get("likes") as? List<String> ?: emptyList()
                        val likeCount = likesList.size
                        val likedByCurrentUser = auth.currentUser?.uid in likesList

                        val savesList = document.get("saves") as? List<String> ?: emptyList()
                        val savedByCurrentUser = auth.currentUser?.uid in savesList

                        val item = ItemForPost(userPhoto, userName, time, title, content, documentId, likeCount, likedByCurrentUser, savedByCurrentUser)
                        postList.add(item)
                    }

                    val removedDocumentIds = addedDocumentIds - documents.map { it.id }
                    for (removedDocumentId in removedDocumentIds) {
                        val removedIndex = postList.indexOfFirst { it.documentId == removedDocumentId }
                        if (removedIndex != -1) {
                            postList.removeAt(removedIndex)
                        }
                    }
                    // Yeni sıralama kriterine göre itemList'i sırala
                    //postList.sortByDescending { getDateFromDateString(it.time) }

                    // Adaptera değişikliği bildir
                    accountPostsRecyclerViewAdapter.notifyDataSetChanged()
                }
                .addOnFailureListener { exception ->
                    Log.w("AccountPostsFragment", "Error getting documents: ", exception)
                }
        }
    }

    private fun loadAccountSaved(currentUserUid: String?) {
        if (currentUserUid != null) {
            db.collection("forum")
                .whereArrayContains("saves", currentUserUid)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val userPhoto = document.getString("userPhoto") ?: ""
                        val title = document.getString("title") ?: ""
                        val content = document.getString("content") ?: ""
                        val userName = document.getString("username") ?: ""
                        val documentId = document.id
                        val timestamp = document.getTimestamp("timestamp")
                        val time = if (timestamp != null) {
                            val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                            sdf.format(timestamp.toDate())
                        } else {
                            ""
                        }
                        val likesList = document.get("likes") as? List<String> ?: emptyList()
                        val likeCount = likesList.size
                        val likedByCurrentUser = auth.currentUser?.uid in likesList

                        val savesList = document.get("saves") as? List<String> ?: emptyList()
                        val savedByCurrentUser = auth.currentUser?.uid in savesList

                        val item = ItemForPost(userPhoto, userName, time, title, content, documentId, likeCount, likedByCurrentUser, savedByCurrentUser)
                        postList.add(item)
                    }

                    // Adaptera değişikliği bildir
                    accountPostsRecyclerViewAdapter.notifyDataSetChanged()
                }
                .addOnFailureListener { exception ->
                    Log.w("AccountPostsFragment", "Error getting documents: ", exception)
                }
        }
    }



    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AccountSavedFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AccountSavedFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}