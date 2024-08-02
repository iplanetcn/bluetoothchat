package com.phenix.bluetoothchat

import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Context.BLUETOOTH_SERVICE
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.viewbinding.ViewBinding
import com.phenix.bluetoothchat.databinding.ItemBluetoothDeviceDiscoveryBinding
import com.phenix.bluetoothchat.databinding.ItemBluetoothDevicePairedBinding

private const val VIEW_TYPE_PAIRED = 1
private const val VIEW_TYPE_DISCOVERY = 2

class BluetoothListAdapter(private var data: List<BluetoothDevice>) : RecyclerView.Adapter<BaseViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return if (viewType == VIEW_TYPE_PAIRED) {
            PairedBluetoothDeviceViewHolder(
                ItemBluetoothDevicePairedBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        } else {
            DiscoveryBluetoothDeviceViewHolder(
                ItemBluetoothDeviceDiscoveryBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val device = data[position]
        holder.bind(device)
    }

    override fun getItemViewType(position: Int): Int {
        val device = data[position]
        return if (device.bondState == BluetoothDevice.BOND_BONDED) {
            VIEW_TYPE_PAIRED
        } else {
            VIEW_TYPE_DISCOVERY
        }
    }
}

abstract class BaseViewHolder(binding: ViewBinding) : ViewHolder(binding.root) {
    abstract fun bind(device: BluetoothDevice)
}

class PairedBluetoothDeviceViewHolder(private val binding: ItemBluetoothDevicePairedBinding) : BaseViewHolder(binding) {
    override fun bind(device: BluetoothDevice) {
        binding.tvName.text = device.name
        binding.tvMacAddress.text = device.address
        binding.tvType.text = getType(device.type)
        binding.ivDeviceType.setImageResource(getDeviceType(device.bluetoothClass))
        val isDeviceConnected = isDeviceConnected(itemView.context, device)
        binding.tvStatus.text = if(isDeviceConnected) "connected" else "not connect"
        if (isDeviceConnected) {
            binding.btnAction.text = itemView.context.getString(R.string.disconnect)
        } else {
            binding.btnAction.text = itemView.context.getString(R.string.connect)
        }
    }
}

class DiscoveryBluetoothDeviceViewHolder(private val binding: ItemBluetoothDeviceDiscoveryBinding) : BaseViewHolder(binding) {
    override fun bind(device: BluetoothDevice) {
        binding.tvName.text = device.name
        binding.tvMacAddress.text = device.address
        binding.tvType.text = getType(device.type)
        binding.ivDeviceType.setImageResource(getDeviceType(device.bluetoothClass))
    }
}

private fun isDeviceConnected(context: Context, device: BluetoothDevice): Boolean {
    val bluetoothManager = context.getSystemService(BLUETOOTH_SERVICE) as android.bluetooth.BluetoothManager
    val connectedDevices = bluetoothManager.getConnectedDevices(android.bluetooth.BluetoothProfile.GATT)
    return connectedDevices.contains(device)
}


private fun getType(type: Int): String {
    return when (type) {
        BluetoothDevice.DEVICE_TYPE_CLASSIC -> "Classic"
        BluetoothDevice.DEVICE_TYPE_LE -> "Low Energy"
        BluetoothDevice.DEVICE_TYPE_DUAL -> "Dual"
        BluetoothDevice.DEVICE_TYPE_UNKNOWN -> "Unknown"
        else -> "Unknown"
    }
}

private fun getDeviceType(bluetoothClass: BluetoothClass?): Int {
    return when (bluetoothClass?.deviceClass) {
        BluetoothClass.Device.COMPUTER_DESKTOP -> R.drawable.ic_desktop
        BluetoothClass.Device.COMPUTER_LAPTOP -> R.drawable.ic_laptop
        BluetoothClass.Device.PHONE_SMART,
        BluetoothClass.Device.PHONE_CELLULAR -> R.drawable.ic_phone
        BluetoothClass.Device.AUDIO_VIDEO_HEADPHONES -> R.drawable.ic_headphones
        BluetoothClass.Device.AUDIO_VIDEO_WEARABLE_HEADSET -> R.drawable.ic_headset
        else -> R.drawable.ic_bluetooth
    }
}