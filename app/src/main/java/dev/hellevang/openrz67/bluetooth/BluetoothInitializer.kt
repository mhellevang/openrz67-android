package dev.hellevang.openrz67.bluetooth

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts

class BluetoothInitializer(private val activity: ComponentActivity) {
    
    fun initialize() {
        val bluetoothManager = activity.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter
        
        val takeResultLauncher = activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                Toast.makeText(activity.applicationContext, "Bluetooth ON", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(activity.applicationContext, "Bluetooth Off", Toast.LENGTH_LONG).show()
            }
        }
        
        val takePermission = activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                if (bluetoothAdapter?.isEnabled != true) {
                    // Request to enable Bluetooth
                    val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    takeResultLauncher.launch(intent)
                } else {
                    Toast.makeText(activity.applicationContext, "Bluetooth is already enabled", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(
                    activity.applicationContext,
                    "Bluetooth Permission is not Granted",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        
        takePermission.launch(Manifest.permission.BLUETOOTH_CONNECT)
    }
}