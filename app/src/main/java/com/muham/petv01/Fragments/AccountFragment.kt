package com.muham.petv01.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.FragmentManager
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.muham.petv01.Adapters.AccountFragmentPageAdapter
import com.muham.petv01.Adapters.FragmentPageAdapter
import com.muham.petv01.BottomSheets.AccountSettingBottomSheetFragment
import com.muham.petv01.R

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AccountFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AccountFragment : Fragment() {
    private lateinit var accountViewPager: ViewPager2
    private lateinit var accountBottomNavigationView: BottomNavigationView
    private lateinit var accountFragmentPageAdapter: AccountFragmentPageAdapter
    private lateinit var settingImageView: ImageView
    // TODO: Rename and change types of parameters
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
        val view = inflater.inflate(R.layout.fragment_account, container, false)

        accountViewPager = view.findViewById(R.id.accountViewPager)
        accountBottomNavigationView = view.findViewById(R.id.accountBottomNavigationView)
        accountFragmentPageAdapter = AccountFragmentPageAdapter(requireActivity().supportFragmentManager, lifecycle)
        settingImageView = view.findViewById<ImageView>(R.id.settingsImageView)

        accountBottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.itPet -> accountViewPager.currentItem = 0
                R.id.itPost -> accountViewPager.currentItem = 1
                R.id.itSave -> accountViewPager.currentItem = 2

            }
            true
        }

        accountViewPager.adapter = accountFragmentPageAdapter

        settingImageView.setOnClickListener {
            // AccountSettingBottomSheetFragment objesini oluştur
            val accountSettingBottomSheetFragment = AccountSettingBottomSheetFragment()

            // Alt sayfayı göster
            accountSettingBottomSheetFragment.show(parentFragmentManager, accountSettingBottomSheetFragment.tag)
        }

        return view
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AccountFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AccountFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}