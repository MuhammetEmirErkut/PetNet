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

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class AccountPostsFragment : Fragment() {

    private lateinit var accountPostsRecyclerView: RecyclerView
    private lateinit var accountPostsSwipeRefreshLayout: SwipeRefreshLayout
    private lateinit var accountPostsRecyclerViewAdapter: ForumPostRecyclerViewAdapter
    private lateinit var postList: MutableList<ItemForPost>

    private val db = FirebaseFirestore.getInstance()
    private lateinit var auth: FirebaseAuth

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
        val view = inflater.inflate(R.layout.fragment_account_posts, container, false)

        postList = mutableListOf()

        accountPostsRecyclerView = view.findViewById(R.id.accountPostsRecyclerView)
        accountPostsSwipeRefreshLayout = view.findViewById(R.id.accountPostsSwipeResfresh)
        accountPostsRecyclerViewAdapter = ForumPostRecyclerViewAdapter(postList)
        accountPostsRecyclerView.adapter = accountPostsRecyclerViewAdapter
        accountPostsRecyclerView.layoutManager = LinearLayoutManager(activity)

        // Kullanıcının kendi UID'sini al
        val currentUserUid = auth.currentUser?.uid

        accountPostsSwipeRefreshLayout.setOnRefreshListener {
            refreshForumData(currentUserUid)

            accountPostsSwipeRefreshLayout.isRefreshing = false
        }

        // Kullanıcının UID'sini kullanarak kendi postlarını getir
        loadAccountPosts(currentUserUid)

        return view
    }
    private fun refreshForumData(currentUserUid: String?) {
        val addedDocumentIds = mutableListOf<String>()

        // Daha önce eklenmiş belgelerin kimliklerini al
        for (item in postList) {
            addedDocumentIds.add(item.documentId)
        }

        if (currentUserUid != null) {
            db.collection("forum")
                .whereEqualTo("author", currentUserUid)
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
                    // Adaptera değişikliği bildir
                    accountPostsRecyclerViewAdapter.notifyDataSetChanged()
                }
                .addOnFailureListener { exception ->
                    Log.w("AccountPostsFragment", "Error getting documents: ", exception)
                }
        }
    }

    private fun loadAccountPosts(currentUserUid: String?) {
        if (currentUserUid != null) {
            db.collection("forum")
                .whereEqualTo("author", currentUserUid)
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
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AccountPostsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
