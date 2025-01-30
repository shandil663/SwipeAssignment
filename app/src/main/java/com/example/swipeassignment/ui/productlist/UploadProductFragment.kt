package com.example.swipeassignment.ui.productlist

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
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
import com.google.gson.JsonParser
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
import java.io.FileOutputStream

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

    override fun onResume() {
        super.onResume()
        // Check if network is available and upload saved products if data exists
        if (isNetworkAvailable()) {
            uploadSavedProducts()
        }
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
                    saveProductLocally(productName, productType, productPrice, productTax, selectedImageUri!!)
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
            val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData("files[]", imageFile.name, requestFile)

            val namePart = createRequestBody(productName)
            val typePart = createRequestBody(productType)
            val pricePart = createRequestBody(price)
            val taxPart = createRequestBody(tax)

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
                        val responseBody = response.body()
                        Log.d("UploadProduct", "Response Code: ${response.code()}")
                        Log.d("UploadProduct", "Response Body: $responseBody")

                        if (responseBody?.success == true) {
                            showToast("Uploaded successfully")
                        } else {
                            showToast("Error: ${responseBody?.message ?: "Unknown error"}")
                        }
                    } else {
                        Log.e("UploadProduct", "Error Response Code: ${response.code()}")
                        Log.e("UploadProduct", "Error Response: ${response.errorBody()?.string()}")
                        showToast("Error: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<ProductResponse>, t: Throwable) {
                    Log.e("UploadProduct", "Request failed: ${t.localizedMessage}", t)
                    showToast("Error: ${t.localizedMessage}")
                }
            })
        } else {
            showToast("Image file not found")
        }
    }
    private fun saveProductLocally(productName: String, productType: String, price: String, tax: String, imageUri: Uri) {
        val sharedPreferences = requireContext().getSharedPreferences("ProductStorage", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Save the image file path instead of URI
        val imageFilePath = saveImageToAppStorage(imageUri)

        // Save product details including the image path
        val productJson = """
        {
            "productName": "$productName",
            "productType": "$productType",
            "price": "$price",
            "tax": "$tax",
            "imagePath": "$imageFilePath"
        }
    """
        val uniqueKey = "product_${System.currentTimeMillis()}"
        editor.putString(uniqueKey, productJson)
        editor.apply()

        showToast("Product saved locally for upload")
    }

    fun uploadSavedProducts() {
        showToast("Uploading pending data")
        val sharedPreferences = requireContext().getSharedPreferences("ProductStorage", Context.MODE_PRIVATE)
        val allProducts = sharedPreferences.all

        allProducts.forEach { entry ->
            val productJson = entry.value.toString()

            // Extract product data from JSON
            val productName = extractProductName(productJson)
            val productType = extractProductType(productJson)
            val productPrice = extractProductPrice(productJson)
            val productTax = extractProductTax(productJson)
            val productImagePath = extractProductImagePath(productJson)

            // Log the image path
            Log.d("UploadProduct", "Extracted Image Path: $productImagePath")

            if (productImagePath.isNotEmpty()) {
                val productImageFile = File(productImagePath)
                if (productImageFile.exists()) {
                    uploadProduct(productName, productType, productPrice, productTax, Uri.fromFile(productImageFile))
                } else {
                    showToast("Image file not found at path: $productImagePath")
                }
            } else {
                showToast("Invalid image path")
            }

            // Remove uploaded product from local storage
            sharedPreferences.edit().remove(entry.key).apply()
        }
    }


    private fun extractProductImagePath(json: String): String {
        val jsonObject = JsonParser.parseString(json).asJsonObject
        return jsonObject.get("imagePath")?.asString ?: ""
    }


    private fun extractProductName(json: String): String {
        val jsonObject = JsonParser.parseString(json).asJsonObject
        return jsonObject.get("productName")?.asString ?: "Unknown"
    }

    private fun extractProductType(json: String): String {
        val jsonObject = JsonParser.parseString(json).asJsonObject
        return jsonObject.get("productType")?.asString ?: "Unknown"
    }

    private fun extractProductPrice(json: String): String {
        val jsonObject = JsonParser.parseString(json).asJsonObject
        return jsonObject.get("price")?.asString ?: "0"
    }

    private fun extractProductTax(json: String): String {
        val jsonObject = JsonParser.parseString(json).asJsonObject
        return jsonObject.get("tax")?.asString ?: "0"
    }

    private fun extractProductImageUri(json: String): String {
        val jsonObject = JsonParser.parseString(json).asJsonObject
        return jsonObject.get("imageUri")?.asString ?: "unknownUri"
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        return networkCapabilities != null && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun getRealPathFromURI(uri: Uri): String? {
        val cursor = requireContext().contentResolver.query(uri, null, null, null, null)
        cursor?.let {
            val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            val filePath = cursor.getString(columnIndex)
            cursor.close()
            return filePath
        }
        return null
    }


    private fun createRequestBody(data: String): RequestBody {
        return data.toRequestBody("text/plain".toMediaTypeOrNull())
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
    private fun saveImageToAppStorage(uri: Uri): String {
        val inputStream = requireContext().contentResolver.openInputStream(uri)
        val file = File(requireContext().filesDir, "product_image_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(file)

        try {
            inputStream?.copyTo(outputStream)
            outputStream.close()
            inputStream?.close()

            Log.d("UploadProduct", "Image saved to: ${file.absolutePath}")
            return file.absolutePath // Return the saved file path
        } catch (e: Exception) {
            Log.e("UploadProduct", "Error saving image: ${e.message}", e)
            return "" // Return empty if saving fails
        }
    }

}

