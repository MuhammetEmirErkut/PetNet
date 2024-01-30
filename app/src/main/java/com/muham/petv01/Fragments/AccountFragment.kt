package com.muham.petv01.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.muham.petv01.Adapters.AccountFragmentPageAdapter
import com.muham.petv01.BottomSheets.AccountSettingBottomSheetFragment
import com.muham.petv01.Fragments.AccountFragments.AccountPetAddFragment
import com.muham.petv01.R

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class AccountFragment : Fragment() {
    private lateinit var accountViewPager: ViewPager2
    private lateinit var accountBottomNavigationView: BottomNavigationView
    private lateinit var accountFragmentPageAdapter: AccountFragmentPageAdapter
    private lateinit var settingImageView: ImageView
    private lateinit var accountPetAddTextView: TextView
    private lateinit var usernameTextView: TextView  // Ekledim

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firebaseFirestore = FirebaseFirestore.getInstance()

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
        settingImageView = view.findViewById(R.id.settingsImageView)
        accountPetAddTextView = view.findViewById(R.id.accountPetAddTextView)
        usernameTextView = view.findViewById(R.id.firstNameLastNameTextView)  // Ekledim

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
            val accountSettingBottomSheetFragment = AccountSettingBottomSheetFragment()
            accountSettingBottomSheetFragment.show(parentFragmentManager, accountSettingBottomSheetFragment.tag)
        }

        accountPetAddTextView.setOnClickListener {
            val accountPetAddFragment = AccountPetAddFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.account_fragment_container, accountPetAddFragment)
                .addToBackStack("AccountFragment")
                .commit()
        }

        // Kullanıcının verilerini çek ve usernameTextView'e ekle
        fetchUserData()

        return view
    }
    private fun fetchUserData() {
        val userId = firebaseAuth.currentUser?.uid

        if (userId != null) {
            firebaseFirestore.collection("Persons").document(userId)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val firstName = documentSnapshot.getString("firstName") ?: ""
                        val lastName = documentSnapshot.getString("lastName") ?: ""
                        val fullName = "$firstName $lastName"
                        usernameTextView.text = fullName
                    }
                }
                .addOnFailureListener { e ->
                    // Hata durumunda burada işlem yapabilirsiniz
                }
        }
    }

    companion object {
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
