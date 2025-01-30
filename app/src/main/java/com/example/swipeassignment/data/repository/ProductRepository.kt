package com.example.swipeassignment.data.repository

import com.example.swipeassignment.data.network.ProductApiService
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody

class ProductRepository(private val apiService: ProductApiService) {
    fun getProducts() = apiService.getProducts()
    fun addProduct(productName: RequestBody, productType:RequestBody, price: RequestBody, tax: RequestBody, image: MultipartBody.Part?) =
        apiService.addProduct(productName, productType, price, tax, image)
}
