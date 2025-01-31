package com.example.swipeassignment.ui.productlist

import android.Manifest
import android.content.Context
import android.content.Intent
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
import com.example.swipeassignment.FragmentAdapter
import com.example.swipeassignment.MainActivity
import com.example.swipeassignment.R
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

class UploadProductFragment : Fragment() {

    private lateinit var binding: FragmentUploadProductBinding
    private var selectedImageUri: Uri? = null
    private val IMAGE_PICK_CODE = 1000
    private val PERMISSION_REQUEST_CODE = 1001
    private var isUploading = false
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
        if (isNetworkAvailable()) {
            uploadOfflineSavedProducts()
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
                return
            }

            val isConnected = isNetworkAvailable()

            if (isConnected) {
                uploadProduct(productName, productType, productPrice, productTax, selectedImageUri)
            } else {
                saveProductLocally(productName, productType, productPrice, productTax)
            }
        } else {
            showToast("All fields are required")
        }
    }


    private fun checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                PERMISSION_REQUEST_CODE
            )
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

    private fun uploadProduct(
        productName: String,
        productType: String,
        price: String,
        tax: String,
        imageUri: Uri?
    ) {
        showProgressBar(true)

        val namePart = createRequestBody(productName)
        val typePart = createRequestBody(productType)
        val pricePart = createRequestBody(price)
        val taxPart = createRequestBody(tax)

        val imagePart = if (imageUri != null) {
            val imagePath = getRealPathFromURI(imageUri)
            val imageFile = File(imagePath)
            if (imageFile.exists()) {
                val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("files[]", imageFile.name, requestFile)
            } else {
                getDefaultImagePart()
            }
        } else {
            getDefaultImagePart()
        }

        val call = apiService.addProduct(namePart, typePart, pricePart, taxPart, imagePart)

        call.enqueue(object : Callback<ProductResponse> {
            override fun onResponse(
                call: Call<ProductResponse>,
                response: Response<ProductResponse>
            ) {
                showProgressBar(false)
                if (response.isSuccessful) {
                    showToast("Uploaded successfully")
                    clearFields()
                } else {
                    showToast("Error: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<ProductResponse>, t: Throwable) {
                showProgressBar(false)
                showToast("Error: ${t.localizedMessage}")
            }
        })
    }

    private fun getDefaultImagePart(): MultipartBody.Part {
        val drawableId = R.drawable.load
        val drawable = ContextCompat.getDrawable(requireContext(), drawableId)
            ?: return MultipartBody.Part.createFormData("files[]", "", "".toRequestBody())

        val bitmap = (drawable as? android.graphics.drawable.BitmapDrawable)?.bitmap
            ?: return MultipartBody.Part.createFormData("files[]", "", "".toRequestBody())

        val file = File(requireContext().cacheDir, "default_image.jpg")
        file.outputStream().use { outputStream ->
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 100, outputStream)
        }

        val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData("files[]", file.name, requestFile)
    }

    private fun saveProductLocally(
        productName: String,
        productType: String,
        price: String,
        tax: String
    ) {
        val sharedPreferences =
            requireContext().getSharedPreferences("ProductStorage", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        val productJson = """
        {
            "productName": "$productName",
            "productType": "$productType",
            "price": "$price",
            "tax": "$tax",
            "imageUri": null
        }
    """
        val uniqueKey = "product_${System.currentTimeMillis()}"
        editor.putString(uniqueKey, productJson)
        editor.apply()

        showToast("Product saved locally for upload")
        clearFields()
    }

    private fun uploadOfflineSavedProducts() {
        val sharedPreferences =
            requireContext().getSharedPreferences("ProductStorage", Context.MODE_PRIVATE)
        val allProducts = sharedPreferences.all

        if (allProducts.isEmpty()) {
            return
        }

        showToast("Uploading pending products, please wait")

        allProducts.forEach { entry ->
            val productJson = entry.value.toString()

            val productName = extractJsonField(productJson, "productName")
            val productType = extractJsonField(productJson, "productType")
            val productPrice = extractJsonField(productJson, "price")
            val productTax = extractJsonField(productJson, "tax")

            uploadProduct(productName, productType, productPrice, productTax, null)
            sharedPreferences.edit().remove(entry.key).apply()
        }
    }

    private fun extractJsonField(json: String, field: String): String {
        val jsonObject = JsonParser.parseString(json).asJsonObject
        return jsonObject.get(field)?.asString ?: ""
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
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

    private fun showProgressBar(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        isUploading = show
    }

    private fun clearFields() {
        binding.productName.text.clear()
        binding.productType.text.clear()
        binding.productPrice.text.clear()
        binding.productTax.text.clear()
        binding.productImageView.setImageDrawable(resources.getDrawable(R.drawable.load))
    }
}
