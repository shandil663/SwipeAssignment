package com.example.swipeassignment

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.swipeassignment.ui.productlist.ProductListFragment
import com.example.swipeassignment.ui.productlist.UploadProductFragment

class FragmentAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
    private val fragmentList = mutableListOf<Fragment>()
    private val titleList = mutableListOf<String>()

    override fun getItemCount(): Int = fragmentList.size

    override fun createFragment(position: Int): Fragment {
        return fragmentList[position]
    }

    fun addFragment(fragment: Fragment, title: String) {
        fragmentList.add(fragment)
        titleList.add(title)
    }

    fun getTabTitle(position: Int): String {
        return titleList[position]
    }
}
