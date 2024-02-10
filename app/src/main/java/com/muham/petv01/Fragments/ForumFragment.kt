package com.muham.petv01.Fragments

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.muham.petv01.Adapters.ForumPostRecyclerViewAdapter
import com.muham.petv01.Inheritance.Comment
import com.muham.petv01.Inheritance.ItemForPost
import com.muham.petv01.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class ForumFragment : Fragment() {

    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var forumRecyclerView: RecyclerView
    private lateinit var forumPostRecyclerViewAdapter: ForumPostRecyclerViewAdapter
    private lateinit var itemList: MutableList<ItemForPost>

    private lateinit var searchImageView: ImageView
    private lateinit var searchEditText: EditText

    private val likedPostIds = mutableSetOf<String>()

    // Firestore database
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
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_forum, container, false)

        itemList = mutableListOf()

        forumRecyclerView = view.findViewById(R.id.forumRecyclerView)
        forumPostRecyclerViewAdapter = ForumPostRecyclerViewAdapter(itemList)
        forumRecyclerView.adapter = forumPostRecyclerViewAdapter
        forumRecyclerView.layoutManager = LinearLayoutManager(activity)

        searchImageView = view.findViewById(R.id.searchImageView)
        searchEditText = view.findViewById(R.id.searchEditText)

        auth = FirebaseAuth.getInstance()

        // Call the function to fetch data from Firestore and add it to itemList
        loadForumData()

        // Refresh
        val swipeRefreshLayout: SwipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)

        swipeRefreshLayout.setOnRefreshListener {
            // Perform refresh operations here

            // For example, you can call the loadForumData() function
            refreshForumData()

            // Finish refreshing when done
            swipeRefreshLayout.isRefreshing = false
        }

        // Add OnClickListener within the ForumFragment, inside the onCreateView() function
        val postButton = view.findViewById<ImageView>(R.id.postButton)
        postButton.setOnClickListener {
            val forumPostFragment = ForumPostFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.forum_fragment_container, forumPostFragment)
                .addToBackStack("ForumFragment")
                .commit()
        }

        // Search
        searchImageView.setOnClickListener {
            val searchText = searchEditText.text.toString()
            performSearch(searchText)
        }

        // Inflate the layout for this fragment
        return view
    }

    private fun performSearch(searchText: String) {
        val resultItems = mutableListOf<ItemForPost>()

        // Fetch data from Firestore collection
        db.collection("forum")
            .orderBy("timestamp", Query.Direction.DESCENDING) // DESCENDING: Sort from newest to oldest
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val title = document.getString("title")?.toLowerCase(Locale.getDefault()) ?: ""
                    val content = document.getString("content")?.toLowerCase(Locale.getDefault()) ?: ""

                    // Search in title or content
                    if (title.contains(searchText.toLowerCase(Locale.getDefault())) || content.contains(searchText.toLowerCase(Locale.getDefault()))) {
                        val userPhoto = document.getString("userPhoto") ?: ""
                        val documentId = document.id
                        val userName = document.getString("username") ?: ""
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
                        resultItems.add(item)
                    }
                }

                // Load the updated results into RecyclerView
                updateRecyclerView(resultItems)
            }
            .addOnFailureListener { exception ->
                Log.w("ForumFragment", "Error getting documents: ", exception)
            }
    }

    private fun updateRecyclerView(itemList: List<ItemForPost>) {
        // Clear the existing itemList
        this.itemList.clear()

        // Add items to the itemList
        this.itemList.addAll(itemList)

        // Sort the itemList by timestamp in descending order
        this.itemList.sortByDescending { item ->
            SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).parse(item.time)
        }

        // Notify the adapter of the change
        forumPostRecyclerViewAdapter.notifyDataSetChanged()
    }

    private fun refreshForumData() {
        val addedDocumentIds = mutableListOf<String>()

        // Get the IDs of previously added documents
        for (item in itemList) {
            addedDocumentIds.add(item.documentId)
        }

        db.collection("forum")
            .orderBy("timestamp", Query.Direction.DESCENDING) // DESCENDING: Sort from newest to oldest
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    // Get the document ID
                    val documentId = document.id

                    // If this document has been added before, skip it
                    if (addedDocumentIds.contains(documentId)) {
                        // Update like count and likedByCurrentUser for existing items in itemList
                        val existingItem = itemList.find { it.documentId == documentId }
                        existingItem?.like = (document.get("likes") as? List<String> ?: emptyList()).size
                        existingItem?.likedByCurrentUser = auth.currentUser?.uid in (document.get("likes") as? List<String> ?: emptyList())
                        continue
                    }
                    val userPhoto = document.getString("userPhoto") ?: ""
                    // Get the data and add it to itemList
                    val title = document.getString("title") ?: ""
                    val content = document.getString("content") ?: ""
                    val userName = document.getString("username") ?: ""
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

                    // Remove the item from the list before adding a new document
                    val removedIndex = itemList.indexOfFirst { it.documentId == documentId }
                    if (removedIndex != -1) {
                        itemList.removeAt(removedIndex)
                    }

                    // Add the new document to the list
                    itemList.add(item)
                }

                // Check for removed documents
                val removedDocumentIds = addedDocumentIds - documents.map { it.id }
                for (removedDocumentId in removedDocumentIds) {
                    val removedIndex = itemList.indexOfFirst { it.documentId == removedDocumentId }
                    if (removedIndex != -1) {
                        itemList.removeAt(removedIndex)
                    }
                }

                // Sort itemList based on the new sorting criteria
                itemList.sortByDescending { getDateFromDateString(it.time) }

                // Notify the adapter of the changes
                forumPostRecyclerViewAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.w("ForumFragment", "Error getting documents: ", exception)
            }
    }

    private fun getDateFromDateString(dateString: String): Date {
        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        return sdf.parse(dateString) ?: Date()
    }

    private fun loadForumData() {
        // Fetch data from Firestore collection
        db.collection("forum")
            .orderBy("timestamp", Query.Direction.DESCENDING) // DESCENDING: Sort from newest to oldest
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val userPhoto = document.getString("userPhoto") ?: ""
                    val documentId = document.id
                    // Get the data and add it to itemList
                    val title = document.getString("title") ?: ""
                    val content = document.getString("content") ?: ""
                    val userName = document.getString("username") ?: ""
                    val timestamp = document.getTimestamp("timestamp")
                    val time = if (timestamp != null) {
                        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                        sdf.format(timestamp.toDate())
                    } else {
                        ""
                    }

                    // Check the likes list fetched from Firestore
                    val likesList = document.get("likes") as? List<String> ?: emptyList()
                    val likedByCurrentUser = auth.currentUser?.uid in likesList

                    // Set the like count to the size of the likes list
                    val likeCount = likesList.size
                    val savesList = document.get("saves") as? List<String> ?: emptyList()
                    val savedByCurrentUser = auth.currentUser?.uid in savesList

                    val item = ItemForPost(userPhoto, userName, time, title, content, documentId, likeCount, likedByCurrentUser, savedByCurrentUser)
                    itemList.add(item)
                }
                // Notify the adapter of the changes
                forumPostRecyclerViewAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.w("ForumFragment", "Error getting documents: ", exception)
            }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ForumFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
