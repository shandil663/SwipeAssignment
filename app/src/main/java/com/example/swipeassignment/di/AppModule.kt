package com.example.swipeassignment.di

import com.example.swipeassignment.data.network.ProductApiService
import com.example.swipeassignment.data.repository.ProductRepository
import com.example.swipeassignment.ui.productlist.ProductViewModel
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val appModule = module {
    // Providing the Retrofit service
    single {
        Retrofit.Builder()
            .baseUrl("https://app.getswipe.in/api/public/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(OkHttpClient.Builder().addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }).build())
            .build()
            .create(ProductApiService::class.java)
    }

    // Providing the ProductRepository
    single { ProductRepository(get()) }

    // Providing the ProductViewModel
    viewModel { ProductViewModel(get()) }
}
