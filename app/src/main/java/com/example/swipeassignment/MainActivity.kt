package com.example.swipeassignment

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.swipeassignment.ui.productlist.ProductListFragment
import com.example.swipeassignment.ui.productlist.UploadProductFragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.example.swipeassignment.databinding.ActivityMainBinding
import com.example.swipeassignment.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        startKoin {
            androidContext(this@MainActivity)
            modules(appModule)
        }

        val adapter = FragmentAdapter(this)
        adapter.addFragment(ProductListFragment(), "Product List")
        adapter.addFragment(UploadProductFragment(), "Upload Product")

        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = adapter.getTabTitle(position)
        }.attach()
    }

}
