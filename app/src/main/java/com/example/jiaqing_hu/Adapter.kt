package com.example.jiaqing_hu

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

// Adapter between fragments and pageviewer
class Adapter(activity: FragmentActivity, var list: ArrayList<Fragment>)
    :FragmentStateAdapter(activity) {
    override fun getItemCount(): Int {
        return list.size
    }

    override fun createFragment(position: Int): Fragment {
        return list[position]
    }
}