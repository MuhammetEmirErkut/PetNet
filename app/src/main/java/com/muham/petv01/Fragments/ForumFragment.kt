package com.muham.petv01.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.muham.petv01.Adapters.ForumPostRecyclerViewAdapter
import com.muham.petv01.Inheritance.ItemForPost
import com.muham.petv01.R

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ForumFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ForumFragment : Fragment() {
    // TODO: Rename and change types of parameters

    private lateinit var forumRecyclerView: RecyclerView
    private lateinit var forumPostRecyclerViewAdapter: ForumPostRecyclerViewAdapter
    private lateinit var itemList: List<ItemForPost>

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

        itemList = generateDummyList(20)

        forumRecyclerView = view.findViewById(R.id.forumRecyclerView)
        forumPostRecyclerViewAdapter = ForumPostRecyclerViewAdapter(itemList)
        forumRecyclerView.adapter = forumPostRecyclerViewAdapter
        forumRecyclerView.layoutManager = LinearLayoutManager(activity)


        // Inflate the layout for this fragment
        return view
    }

    private fun generateDummyList(size: Int): List<ItemForPost> {
        val list = ArrayList<ItemForPost>()
        for (i in 0 until size) {
            val item = ItemForPost("Item $i", "UserName $i", "*$i h", "Title $i", "Content $i")
            list += item
        }
        return list
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ForumFragment.
         */
        // TODO: Rename and change types and number of parameters
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