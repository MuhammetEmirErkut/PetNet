package com.muham.petv01.Adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.muham.petv01.Fragments.AccountFragment
import com.muham.petv01.Fragments.ForumFragment
import com.muham.petv01.Fragments.HomeFragment

class FragmentPageAdapter (
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle
) : FragmentStateAdapter(fragmentManager, lifecycle){
    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> HomeFragment()
            1 -> ForumFragment() // ForumPostFragment'ı buraya ekleyin
            2 -> AccountFragment()
            else -> HomeFragment()
        }
    }
}