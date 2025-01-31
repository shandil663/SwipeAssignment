package com.example.swipeassignment.ui.productlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.swipeassignment.databinding.FragmentProductListBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class ProductListFragment : Fragment() {

    private val productViewModel: ProductViewModel by viewModel()
    private lateinit var binding: FragmentProductListBinding
    private lateinit var productAdapter: ProductAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProductListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        productAdapter = ProductAdapter(emptyList())
        binding.productRecyclerView.layoutManager = GridLayoutManager(context, 2)
        binding.productRecyclerView.adapter = productAdapter


        productViewModel.products.observe(viewLifecycleOwner) {
            productAdapter.updateProducts(it)
        }


        productViewModel.error.observe(viewLifecycleOwner) {

        }


        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                productViewModel.filterProducts(newText)
                return true
            }
        })
    }

    override fun onResume() {
        super.onResume()
        productViewModel.fetchProducts()

        productViewModel.products.observe(viewLifecycleOwner) {
            productAdapter.updateProducts(it)
        }
    }
}
