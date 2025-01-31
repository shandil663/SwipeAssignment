package com.example.swipeassignment.ui.productlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.swipeassignment.data.model.Product
import com.example.swipeassignment.data.model.ProductResponse
import com.example.swipeassignment.data.repository.ProductRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import okhttp3.MultipartBody
import okhttp3.RequestBody
class ProductViewModel(private val repository: ProductRepository) : ViewModel() {

    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> get() = _products

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    private val _uploadStatus = MutableLiveData<Boolean>()
    val uploadStatus: LiveData<Boolean> get() = _uploadStatus

    private var allProducts = listOf<Product>()

    fun fetchProducts() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getProducts().enqueue(object : Callback<List<Product>> {
                override fun onResponse(call: Call<List<Product>>, response: Response<List<Product>>) {
                    if (response.isSuccessful) {
                        allProducts = response.body() ?: emptyList()  // Save the full list
                        _products.postValue(allProducts)  // Display the full list
                    } else {
                        _error.postValue("Error: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<List<Product>>, t: Throwable) {
                    _error.postValue(t.localizedMessage)
                }
            })
        }
    }

    fun filterProducts(query: String?) {
        val filteredList = if (query.isNullOrEmpty()) {
            allProducts
        } else {
            allProducts.filter {
                it.product_name.contains(query, ignoreCase = true) ||
                        it.product_type.contains(query, ignoreCase = true)
            }
        }
        _products.postValue(filteredList)
    }

    fun addProduct(productName: RequestBody, productType: RequestBody, price: RequestBody, tax: RequestBody, image: MultipartBody.Part?) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addProduct(productName, productType, price, tax, image).enqueue(object : Callback<ProductResponse> {

                override fun onResponse(call: Call<ProductResponse>, response: Response<ProductResponse>) {
                    if (response.isSuccessful) {
                        _uploadStatus.postValue(true)
                    } else {
                        _uploadStatus.postValue(false)
                    }
                }

                override fun onFailure(call: Call<ProductResponse>, t: Throwable) {
                    _uploadStatus.postValue(false)
                }
            })
        }
    }
}
