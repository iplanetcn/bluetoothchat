package com.phenix.bluetoothchat

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import com.phenix.bluetoothchat.databinding.ActivityMainBinding

const val REQUEST_BLUETOOTH_PERMISSIONS: Int = 1

class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var info: StringBuilder = StringBuilder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        if (!isBluetoothSupport()) {
            Toast.makeText(this, "Your device did not support Bluetooth", Toast.LENGTH_SHORT).show()
            return
        }

        // Check for Bluetooth permissions
        if (!hasPermissions()) {
            // Request necessary permissions
            requestBluetoothPermissions()
        } else {
            // Permissions already granted, proceed with your Bluetooth operations
            initializeBluetooth()
        }
    }

    private fun hasPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        } else {
            checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ), REQUEST_BLUETOOTH_PERMISSIONS
            )
        } else {
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION
                ), REQUEST_BLUETOOTH_PERMISSIONS
            )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_BLUETOOTH_PERMISSIONS) {
            if (allPermissionsGranted(grantResults)) {
                // Permissions granted, proceed with Bluetooth operations
                initializeBluetooth()
            } else {
                // Permissions not granted, show a message to the user and disable Bluetooth functionality
                Toast.makeText(this, "Bluetooth permissions are required to use this feature.", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun allPermissionsGranted(grantResults: IntArray): Boolean {
        for (result in grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    private fun toggleInfo() {
        binding.tvInfo.apply {
            visibility = if (isVisible) View.GONE else View.VISIBLE
        }
    }

    private fun initializeBluetooth() {
        // Initialize Bluetooth components and start Bluetooth operations
        val bluetoothManager: BluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        if (bluetoothAdapter == null || !bluetoothAdapter!!.isEnabled) {
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                if (result.resultCode == Activity.RESULT_OK) {
//                    val intent = result.data
                    // Handle the Intent
                    Log.d("MainActivity", "request bluetooth enable success")
                } else if (result.resultCode == Activity.RESULT_CANCELED) {
                    Toast.makeText(this@MainActivity, "You canceled the operation", Toast.LENGTH_SHORT).show()
                }
            }.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
        }

        info.append("---------- support info ----------\n")
        info.append("Bluetooth: ${isBluetoothSupport()}\n")
        info.append("BLE: ${isBluetoothLESupport()}\n")

        binding.tvInfo.text = info
    }

    private fun isBluetoothSupport(): Boolean {
        // Check to see if the Bluetooth classic feature is available.
        return packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)
    }

    private fun isBluetoothLESupport(): Boolean {
        // Check to see if the BLE feature is available.
        return packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_toggle_info) {
            toggleInfo()
        }
        return super.onOptionsItemSelected(item)
    }


    companion object {
        @JvmStatic
        fun start(context: Context) {
            val starter = Intent(context, MainActivity::class.java).putExtra("start", true)
            context.startActivity(starter)
        }
    }
}