package com.phenix.bluetoothchat

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.phenix.bluetoothchat.databinding.ActivityMainBinding


const val REQUEST_BLUETOOTH_PERMISSIONS: Int = 1

class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var info: StringBuilder = StringBuilder()
    private var pairedDevices: MutableSet<BluetoothDevice> = mutableSetOf()
    private var discoveredDevices: MutableSet<BluetoothDevice> = mutableSetOf()
    private var deviceList: MutableList<BluetoothDevice> = mutableListOf()

    private lateinit var bluetoothListAdapter: BluetoothListAdapter

    // Create a BroadcastReceiver for ACTION_FOUND.
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action: String? = intent.action
            when(action) {
                BluetoothDevice.ACTION_FOUND -> {
                    // Discovery has found a device. Get the BluetoothDevice, object and its info from the Intent.
                    val device: BluetoothDevice? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE,BluetoothDevice::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    }
                    if (device != null && !device.name.isNullOrEmpty()) {
                        discoveredDevices.add(device)
                        updateDeviceList()
                    }
                }
            }
        }
    }

    private fun updateDeviceList() {
        deviceList.clear()
        deviceList.addAll(pairedDevices)
        deviceList.addAll(discoveredDevices)
        bluetoothListAdapter.notifyDataSetChanged()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        bluetoothListAdapter = BluetoothListAdapter(deviceList)
        binding.recyclerView.adapter = bluetoothListAdapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

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

        binding.btnOpenSettings.setOnClickListener {
            AlertDialog.Builder(this)
                .setCancelable(false)
                .setMessage(getString(R.string.message_for_required_permissions))
                .setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                    dialog.dismiss()
                    val uri = Uri.fromParts("package", packageName, null)
                    startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(uri))
                }
                .setNeutralButton(getString(R.string.cancel)) { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }

    override fun onResume() {
        super.onResume()
        if (binding.layoutNoPermissionTips.visibility == View.VISIBLE) {
            binding.layoutNoPermissionTips.visibility = if (hasPermissions()) View.GONE else View.VISIBLE
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_BLUETOOTH_PERMISSIONS) {
            if (allPermissionsGranted(grantResults)) {
                // Permissions granted, proceed with Bluetooth operations
                initializeBluetooth()
            } else {
                // Permissions not granted, show a message to the user and disable Bluetooth functionality
                // Toast.makeText(this, "Bluetooth permissions are required to use this feature.", Toast.LENGTH_SHORT).show()
                binding.layoutNoPermissionTips.visibility = View.VISIBLE
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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return
        }

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

        bluetoothAdapter?.apply {
            pairedDevices = bondedDevices
            updateDeviceList()
            registerReceiver(receiver, IntentFilter(BluetoothDevice.ACTION_FOUND))
            startDiscovery()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        bluetoothAdapter?.cancelDiscovery()
        unregisterReceiver(receiver)
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