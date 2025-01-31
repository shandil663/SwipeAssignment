Swipe Assignment 📱
Swipe Assignment is an Android application that allows users to view and upload product information. The app uses Kotlin and XML to create a smooth user experience. The app fetches products from an API and allows users to upload products, with or without an image, based on network availability.

🚀 Technologies Used

Core Technologies:
Kotlin: A modern, statically typed language for Android development.
XML: Used for creating user interface layouts.

Architecture:
MVVM (Model-View-ViewModel): To separate UI logic from business logic, making the app more maintainable.
ViewModel: To manage UI-related data in a lifecycle-conscious way.
LiveData: Used for data observation to update UI components.

Libraries:
Retrofit: For making HTTP requests to interact with the API.
OkHttp: A powerful HTTP client for sending and receiving network requests.
Gson: A library for serializing and deserializing JSON data.
Koin: A lightweight dependency injection framework for Kotlin.
ViewPager2: For adding swipeable tabs and fragments.

UI/UX:
Material Design: Implements Google's Material Design components for modern UI design.
SearchView: For filtering products.
GridLayoutManager: For displaying products in a grid.

Permissions:
READ_EXTERNAL_STORAGE: For accessing images from the device storage.

Networking:
ConnectivityManager: For checking the network status and conditionally uploading products.
📦 Getting Started
To get started with the project locally, follow the steps below:

Prerequisites:
Make sure you have the following installed on your system:

Android Studio (latest version)
Kotlin (integrated with Android Studio)
JDK 8 or above

Clone the Repository:
Clone the repository to your local machine using the following command:
git clone https://github.com/your-username/SwipeAssignment.git

Open the Project in Android Studio:
Open Android Studio.
Click on File → Open → Choose the folder where you cloned the project.
Android Studio will automatically sync the project and download the necessary dependencies.

Run the Project:
Connect your Android device or start an emulator.
Click on the Run button in Android Studio or use the shortcut Shift + F10.
The app should now launch on your device/emulator.

Important Files:
MainActivity.kt: The main activity that initializes and sets up fragments.
FragmentAdapter.kt: Adapter for managing fragments in the view pager.
ProductListFragment.kt: Displays a list of products using a RecyclerView.
UploadProductFragment.kt: Allows users to upload product details and images.

💡 Features
View Products: Users can browse through a list of products.
Upload Products: Users can add new products by filling out a form and uploading an image.
Offline Storage: Products are saved locally if there is no internet connection.
Network Handling: The app checks if the network is available before attempting to upload products.

📸 Screenshots
Product List Screen

![1](https://github.com/user-attachments/assets/8ac0b4af-a6cd-40fb-a2c0-6b72c59bd0bb)
![5](https://github.com/user-attachments/assets/1ca4d8fb-4aca-4af3-86ae-b4147aaaa713)
![4](https://github.com/user-attachments/assets/db955eb9-1fcf-4bc4-a93d-d9b5014acce3)
![3](https://github.com/user-attachments/assets/a820abe5-98a0-4d1f-9270-a8d9dd0f52a0)
![2](https://github.com/user-attachments/assets/560311c5-99f4-4bf7-9aae-03dfae885a6f)



📱 App Structure
SwipeAssignment/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   ├── com.example.swipeassignment/
│   │   │   │   │   ├── MainActivity.kt
│   │   │   │   │   ├── FragmentAdapter.kt
│   │   │   │   │   ├── ui/
│   │   │   │   │   │   ├── productlist/
│   │   │   │   │   │   │   ├── ProductListFragment.kt
│   │   │   │   │   │   │   ├── UploadProductFragment.kt
│   │   │   ├── res/
│   │   │   │   ├── layout/
│   │   │   │   │   ├── activity_main.xml
│   │   │   │   │   ├── fragment_product_list.xml
│   │   │   │   │   ├── fragment_upload_product.xml
│   │   │   │   ├── values/
│   │   │   │   │   ├── strings.xml
│   │   │   │   │   ├── colors.xml
🔧 How It Works
View Products: The ProductListFragment fetches products from the server or local storage and displays them in a RecyclerView.
Upload Products: The UploadProductFragment allows users to fill in product details (name, type, price, tax) and upload an image. If the network is available, the product is uploaded to the server; otherwise, it is saved locally.
Network Handling: The app checks network availability before attempting to upload data.
