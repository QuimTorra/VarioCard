//package com.degref.variocard
//
//import android.Manifest
//import android.content.pm.PackageManager
//import android.os.Build
//import android.os.Bundle
//import android.view.View
//import androidx.activity.compose.setContent
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Surface
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.core.app.ActivityCompat
//import com.degref.variocard.ui.theme.VarioCardTheme
//import com.degref.variocard.wifiDirect.sender.FileSenderActivity
//import com.degref.variocard.wifiDirect.receiver.FileReceiverActivity
//
//class MainActivity : BaseActivity() {
//    private val requestedPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//        arrayOf(
//            Manifest.permission.ACCESS_NETWORK_STATE,
//            Manifest.permission.CHANGE_NETWORK_STATE,
//            Manifest.permission.ACCESS_WIFI_STATE,
//            Manifest.permission.CHANGE_WIFI_STATE,
//            Manifest.permission.NEARBY_WIFI_DEVICES
//        )
//    } else {
//        arrayOf(
//            Manifest.permission.ACCESS_NETWORK_STATE,
//            Manifest.permission.CHANGE_NETWORK_STATE,
//            Manifest.permission.ACCESS_WIFI_STATE,
//            Manifest.permission.CHANGE_WIFI_STATE,
//            Manifest.permission.ACCESS_COARSE_LOCATION,
//            Manifest.permission.ACCESS_FINE_LOCATION
//        )
//    }
//
//    private val requestPermissionLaunch = registerForActivityResult(
//        ActivityResultContracts.RequestMultiplePermissions()
//    ) { it ->
//        if (it.all { it.value }) {
//            showToast("All permissions obtained")
//        } else {
//            onPermissionDenied()
//        }
//    }
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        findViewById<View>(R.id.btnCheckPermission).setOnClickListener {
//            requestPermissionLaunch.launch(requestedPermissions)
//        }
//        findViewById<View>(R.id.btnSender).setOnClickListener {
//            if (allPermissionGranted()) {
//                startActivity(FileSenderActivity::class.java)
//            } else {
//                onPermissionDenied()
//            }
//        }
//        findViewById<View>(R.id.btnReceiver).setOnClickListener {
//            if (allPermissionGranted()) {
//                startActivity(FileReceiverActivity::class.java)
//            } else {
//                onPermissionDenied()
//            }
//        }
//        setContent {
//            VarioCardTheme {
//                // A surface container using the 'background' color from the theme
//                Surface(
//                    modifier = Modifier.fillMaxSize(),
//                    color = MaterialTheme.colorScheme.background
//                ) {
//                    Greeting("Android")
//                }
//            }
//        }
//    }
//
//    private fun onPermissionDenied() {
//        showToast("Lack of permissions, please grant permissions first")
//    }
//
//    private fun allPermissionGranted(): Boolean {
//        requestedPermissions.forEach {
//            if (ActivityCompat.checkSelfPermission(
//                    this,
//                    it
//                ) != PackageManager.PERMISSION_GRANTED
//            ) {
//                return false
//            }
//        }
//        return true
//    }
//}
//
//@Composable
//fun Greeting(name: String, modifier: Modifier = Modifier) {
//    Text(
//        text = "Hello $name!",
//        modifier = modifier
//    )
//}
//
//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    VarioCardTheme {
//        Greeting("Android")
//    }
//}