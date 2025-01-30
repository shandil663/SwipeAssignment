package com.example.swipeassignment.data.network

import com.example.swipeassignment.data.model.Product
import com.example.swipeassignment.data.model.ProductResponse
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
interface ProductApiService {
    @GET("get")
    fun getProducts(): Call<List<Product>>

    @Multipart
    @POST("add")
    fun addProduct(
        @Part("product_name") productName: RequestBody,
        @Part("product_type") productType: RequestBody,
        @Part("price") price: RequestBody,
        @Part("tax") tax: RequestBody,
        @Part files: MultipartBody.Part? = null

    ): Call<ProductResponse>
}
