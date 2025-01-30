package com.example.swipeassignment.ui.productlist

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import com.example.swipeassignment.data.model.ProductResponse
import com.example.swipeassignment.data.network.ProductApiService
import com.example.swipeassignment.databinding.FragmentUploadProductBinding
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File

class UploadProductFragment : Fragment() {

    private lateinit var binding: FragmentUploadProductBinding
    private var selectedImageUri: Uri? = null
    private val IMAGE_PICK_CODE = 1000
    private val PERMISSION_REQUEST_CODE = 1001

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://app.getswipe.in/api/public/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService: ProductApiService = retrofit.create(ProductApiService::class.java)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUploadProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.selectImageButton.setOnClickListener { checkAndRequestPermissions() }
        binding.uploadButton.setOnClickListener { handleUploadButtonClick() }
    }

    private fun handleUploadButtonClick() {
        val productName = binding.productName.text.toString()
        val productType = binding.productType.text.toString()
        val productPrice = binding.productPrice.text.toString()
        val productTax = binding.productTax.text.toString()

        if (productName.isNotEmpty() && productType.isNotEmpty() && productPrice.isNotEmpty() && productTax.isNotEmpty()) {
            if (selectedImageUri == null) {
                showToast("Please select an image")
            } else {
                val isConnected = isNetworkAvailable()
                if (isConnected) {
                    uploadProduct(productName, productType, productPrice, productTax, selectedImageUri!!)
                } else {
                    showToast("No network available. Please check your connection.")
                }
            }
        } else {
            showToast("All fields are required")
        }
    }

    private fun checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSION_REQUEST_CODE)
        } else {
            openImagePicker()
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == AppCompatActivity.RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            selectedImageUri = data?.data
            binding.productImageView.setImageURI(selectedImageUri)
        }
    }
    private fun uploadProduct(productName: String, productType: String, price: String, tax: String, imageUri: Uri) {
        val imagePath = getRealPathFromURI(imageUri)
        val imageFile = File(imagePath)

        if (imageFile.exists()) {
            // Create RequestBody for the image
            val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData("files[]", imageFile.name, requestFile)


            // Create RequestBody for other product details
            val namePart = createRequestBody(productName)
            val typePart = createRequestBody(productType)
            val pricePart = createRequestBody(price)
            val taxPart = createRequestBody(tax)

            // Make the API call
            val call = apiService.addProduct(
                namePart,
                typePart,
                pricePart,
                taxPart,
                imagePart
            )

            call.enqueue(object : Callback<ProductResponse> {
                override fun onResponse(call: Call<ProductResponse>, response: Response<ProductResponse>) {
                    if (response.isSuccessful) {
                        // Successfully uploaded the product
                        val responseBody = response.body()
                        Log.d("UploadProduct", "Response Code: ${response.code()}")
                        Log.d("UploadProduct", "Response Body: $responseBody")

                        if (responseBody?.success == true) {
                            showToast("Uploaded successfully")
                            Log.d("UploadProduct", "Product ID:")
                            Log.d("UploadProduct", "Product Details:")
                        } else {
                            showToast("Error: ${responseBody?.message ?: "Unknown error"}")
                        }
                    } else {
                        // Log the error response if not successful
                        Log.e("UploadProduct", "Error Response Code: ${response.code()}")
                        Log.e("UploadProduct", "Error Response: ${response.errorBody()?.string()}")
                        showToast("Error: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<ProductResponse>, t: Throwable) {
                    // Log failure details, e.g., network issues, server issues, etc.
                    Log.e("UploadProduct", "Request failed: ${t.localizedMessage}", t)
                    showToast("Error: ${t.localizedMessage}")
                }
            })
        } else {
            showToast("Image file not found")
        }
    }



    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    private fun getRealPathFromURI(uri: Uri): String {
        val context = requireContext()
        val inputStream = context.contentResolver.openInputStream(uri) ?: return ""
        val file = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
        file.outputStream().use { output ->
            inputStream.copyTo(output)
        }
        return file.absolutePath
    }

    private fun createRequestBody(data: String): RequestBody {
        return data.toRequestBody("text/plain".toMediaTypeOrNull())
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
