package com.muham.petv01.Adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.muham.petv01.Fragments.AccountFragments.AccountPetsFragment
import com.muham.petv01.Fragments.AccountFragments.AccountPostsFragment
import com.muham.petv01.Fragments.AccountFragments.AccountSavedFragment

class AccountFragmentPageAdapter (
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle
) : FragmentStateAdapter(fragmentManager, lifecycle){
    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> AccountPetsFragment()
            1 -> AccountPostsFragment() // ForumPostFragment'ı buraya ekleyin
            2 -> AccountSavedFragment()
            else -> AccountPetsFragment()
        }
    }
}