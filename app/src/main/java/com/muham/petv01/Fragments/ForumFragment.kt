package com.muham.petv01.Fragments

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.muham.petv01.Adapters.ForumPostRecyclerViewAdapter
import com.muham.petv01.Inheritance.Comment
import com.muham.petv01.Inheritance.ItemForPost
import com.muham.petv01.R
import java.text.SimpleDateFormat
import java.util.Locale


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
class ForumFragment : Fragment() {


    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var forumRecyclerView: RecyclerView
    private lateinit var forumPostRecyclerViewAdapter: ForumPostRecyclerViewAdapter
    private lateinit var itemList: MutableList<ItemForPost>

    private val likedPostIds = mutableSetOf<String>()



    // Firestore veritabanı
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


        auth = FirebaseAuth.getInstance()



        // Firestore'dan verileri çekip itemList'e ekleyen fonksiyonu çağır
        loadForumData()


        //Refresh
        val swipeRefreshLayout: SwipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)

        swipeRefreshLayout.setOnRefreshListener {
            // Yeniden yükleme işlemlerini burada yap

            // Örneğin, loadForumData() fonksiyonunu çağırabilirsiniz
            refreshForumData()

            // Yeniden yükleme tamamlandığında yenilemeyi bitir
            swipeRefreshLayout.isRefreshing = false
        }
        //////////////////

        // ForumFragment içinde, onCreateView() fonksiyonu içinde onClickListener ekle
        val postButton = view.findViewById<ImageView>(R.id.postButton)
        postButton.setOnClickListener {
            val forumPostFragment = ForumPostFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.forum_fragment_container, forumPostFragment)
                .addToBackStack("ForumFragment")
                .commit()
        }

        // Inflate the layout for this fragment
        return view
    }
    private fun refreshForumData() {
        val addedDocumentIds = mutableListOf<String>()

        // Daha önce eklenmiş belgelerin kimliklerini al
        for (item in itemList) {
            addedDocumentIds.add(item.documentId)
        }

        db.collection("forum")
            .orderBy("timestamp", Query.Direction.DESCENDING) // DESCENDING: Yeniden eskiye doğru sırala
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    // Belge kimliğine eriş
                    val documentId = document.id

                    // Eğer bu belge daha önce eklenmişse, geç
                    if (addedDocumentIds.contains(documentId)) {
                        // Update like count for existing items in itemList
                        val existingItem = itemList.find { it.documentId == documentId }
                        existingItem?.like = (document.get("likes") as? List<String> ?: emptyList()).size
                        continue
                    }

                    // Verileri al ve itemList'e ekle
                    val title = document.getString("title") ?: ""
                    val content = document.getString("content") ?: ""
                    val userName = document.getString("userName") ?: ""
                    val timestamp = document.getTimestamp("timestamp")
                    val time = if (timestamp != null) {
                        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                        sdf.format(timestamp.toDate())
                    } else {
                        ""
                    }
                    val likesList = document.get("likes") as? List<String> ?: emptyList()
                    val likeCount = likesList.size

                    // Belge kimliğini kullanarak yeni bir ItemForPost nesnesi oluştur
                    val item = ItemForPost("null", userName, time, title, content, documentId, likeCount, false)

                    itemList.add(0, item)
                }
                // Adaptera değişikliği bildir
                forumPostRecyclerViewAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.w("ForumFragment", "Error getting documents: ", exception)
            }
    }

    private fun loadForumData() {
        // Firestore koleksiyonundan verileri çek
        db.collection("forum")
            .orderBy("timestamp", Query.Direction.DESCENDING) // DESCENDING: Yeniden eskiye doğru sırala
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val documnetId = document.id
                    // Verileri al ve itemList'e ekle
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

                    // Firestore'dan çekilen likes listesini kontrol et
                    val likesList = document.get("likes") as? List<String> ?: emptyList()
                    val likedByCurrentUser = auth.currentUser?.uid in likesList

                    // Like sayısını likes listesinin eleman sayısı olarak ayarla
                    val likeCount = likesList.size

                    val item = ItemForPost("null", userName, time, title, content, documnetId, likeCount, likedByCurrentUser)
                    itemList.add(item)
                }
                // Adaptera değişikliği bildir
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
