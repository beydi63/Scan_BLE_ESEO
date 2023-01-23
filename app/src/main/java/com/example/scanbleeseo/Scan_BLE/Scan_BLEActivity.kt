package com.example.scanbleeseo.Scan_BLE

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.*
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.scanbleeseo.Adapter.DeviceAdapter
import com.example.scanbleeseo.Permission.Permission_Localisation_refuseeActivity
import com.example.scanbleeseo.R
import org.w3c.dom.Text
import java.util.*


class Scan_BLEActivity : AppCompatActivity() {

    private var bluetoothAdapter: BluetoothAdapter? = null
    private var currentBluetoothGatt: BluetoothGatt? = null
    private var bluetoothLeScanner: BluetoothLeScanner? = null
    private val scanSettings = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()
    private var scanFilters: List<ScanFilter> = arrayListOf(
       ScanFilter.Builder().setServiceUuid(ParcelUuid(BluetoothLEManager.DEVICE_UUID)).build()
    )
    private var mScanning = false
    private val handler = Handler(Looper.getMainLooper())
    private val bleDevicesFoundList = arrayListOf<DeviceAdapter.Device>()
    private val PERMISSION_REQUEST_LOCATION = 99
    private var rvDevices: RecyclerView? = null
    private var ledStatus: ImageView? = null
    private var startScan: Button? = null
    private var currentConnexion: TextView? = null
    private var disconnect: Button? = null
    private var toggleLed: Button? = null



    companion object {
        fun getStartIntent(context: Context): Intent {
            return Intent(context, Scan_BLEActivity::class.java)
        }
    }


    private fun hasPermission(): Boolean {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun askForPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_REQUEST_LOCATION)
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN), PERMISSION_REQUEST_LOCATION)
        }
    }

    private fun locationServiceEnabled(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val lm = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            lm.isLocationEnabled
        } else {
            val mode = Settings.Secure.getInt(this.contentResolver, Settings.Secure.LOCATION_MODE, Settings.Secure.LOCATION_MODE_OFF)
            mode != Settings.Secure.LOCATION_MODE_OFF
        }
    }

    @SuppressLint("MissingPermission")
    private fun setupBLE() {
        (getSystemService(BLUETOOTH_SERVICE) as BluetoothManager?)?.let { bluetoothManager ->
            bluetoothAdapter = bluetoothManager.adapter
            if (bluetoothAdapter != null && !bluetoothManager.adapter.isEnabled) {
                registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                    if (it.resultCode == Activity.RESULT_OK) {
                        scanLeDevice()
                    } else {
                        Toast.makeText(this, "Bluetooth non activé", Toast.LENGTH_SHORT).show()
                    }
                }.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
            } else {
                scanLeDevice()
            }
        }
    }

    private val leScanCallback: ScanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
             val device = DeviceAdapter.Device(result.device.name, result.device.address, result.device)
             if (!device.name.isNullOrBlank() && !bleDevicesFoundList.contains(device)) {
                 bleDevicesFoundList.add(device)
                 findViewById<RecyclerView>(R.id.recycler_view).adapter?.notifyItemInserted(bleDevicesFoundList.size - 1)
             }
        }
    }

    @SuppressLint("MissingPermission")
    private fun scanLeDevice(scanPeriod: Long = 10000) {
        if (!mScanning) {
            bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner
            bleDevicesFoundList.clear()
            mScanning = true
            handler.postDelayed({
                mScanning = false
                bluetoothLeScanner?.stopScan(leScanCallback)
                Toast.makeText(this, getString(R.string.scan_ended), Toast.LENGTH_SHORT).show()
            }, scanPeriod)
            bluetoothLeScanner?.startScan(scanFilters, scanSettings, leScanCallback)
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    if (requestCode == PERMISSION_REQUEST_LOCATION) {
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED && locationServiceEnabled()) {
            setupBLE()
        } else if (!locationServiceEnabled()) {
            startActivity(Permission_Localisation_refuseeActivity.getStartIntent(this))
        } else {
            startActivity(Scan_BLE_refuseeActivity.getStartIntent(this))

            }
        }
    }



    override fun onResume() {
        super.onResume()
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, getString(R.string.not_compatible), Toast.LENGTH_SHORT).show()
            finish()
        } else if (hasPermission() && locationServiceEnabled()) {
            setupBLE()
        } else if(!hasPermission()) {
            askForPermission()
        } else {
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }
    }

    @SuppressLint("MissingPermission")
    private fun connectToCurrentDevice() {
        BluetoothLEManager.currentDevice?.let { device ->
            Toast.makeText(this, "Connexion en cours … $device", Toast.LENGTH_SHORT).show()
            currentBluetoothGatt = device.connectGatt(this, false, BluetoothLEManager.GattCallback(
                onConnect = {
                        runOnUiThread {
                            enableListenBleNotify()
                            setUiMode(true)
                        }
                    },
                    onNotify = {
                        runOnUiThread {
                            handleToggleLedNotificationUpdate(it)
                        }
                   },
                    onDisconnect = { runOnUiThread { disconnectFromCurrentDevice() } })
            )
        }
    }



    @SuppressLint("MissingPermission")
    private fun enableListenBleNotify() {
        getMainDeviceService()?.let { service ->
            Toast.makeText(this, getString(R.string.enable_ble_notifications), Toast.LENGTH_SHORT).show()
            val notification = service.getCharacteristic(BluetoothLEManager.CHARACTERISTIC_NOTIFY_STATE)
            currentBluetoothGatt?.setCharacteristicNotification(notification, true)
        }
    }


    @SuppressLint("MissingPermission")
    private fun disconnectFromCurrentDevice() {
        currentBluetoothGatt?.disconnect()
        BluetoothLEManager.currentDevice = null
        setUiMode(false)
    }
//_____________________________________________________________________________________________________

//_____________________________________________________________________________________________________
    private fun getMainDeviceService(): BluetoothGattService? {
        return currentBluetoothGatt?.let { bleGatt ->
            val service = bleGatt.getService(BluetoothLEManager.DEVICE_UUID)
            service?.let {
                return it
            } ?: run {
                Toast.makeText(this, getString(R.string.uuid_not_found), Toast.LENGTH_SHORT).show()
                return null
            }
        } ?: run {
            Toast.makeText(this, getString(R.string.not_connected), Toast.LENGTH_SHORT).show()
            return null
        }
    }
//_____________________________________________________________________________________________________

    private fun handleToggleLedNotificationUpdate(characteristic: BluetoothGattCharacteristic) {
        if (characteristic.getStringValue(0).equals("on", ignoreCase = true)) {
            ledStatus?.setImageResource(R.drawable.on)
        } else {
            ledStatus?.setImageResource(R.drawable.off)
        }
    }

//_____________________________________________________________________________________________________
    @SuppressLint("MissingPermission")
    private fun toggleLed() {
        getMainDeviceService()?.let { service ->
            val toggleLed = service.getCharacteristic(BluetoothLEManager.CHARACTERISTIC_TOGGLE_LED_UUID)
            toggleLed.setValue("1")
            currentBluetoothGatt?.writeCharacteristic(toggleLed)
        }
    }
//_____________________________________________________________________________________________________
    class BluetoothLEManager {
        companion object {
            var currentDevice: BluetoothDevice? = null
            val DEVICE_UUID: UUID = UUID.fromString("795090c7-420d-4048-a24e-18e60180e23c")
            val CHARACTERISTIC_LED_PIN_UUID: UUID = UUID.fromString("31517c58-66bf-470c-b662-e352a6c80cba")
            val CHARACTERISTIC_BUTTON_PIN_UUID: UUID = UUID.fromString("0b89d2d4-0ea6-4141-86bb-0c5fb91ab14a")
            val CHARACTERISTIC_TOGGLE_LED_UUID: UUID = UUID.fromString("59b6bf7f-44de-4184-81bd-a0e3b30c919b")
            val CHARACTERISTIC_NOTIFY_STATE: UUID = UUID.fromString("d75167c8-e6f9-4f0b-b688-09d96e195f00")
        }
        open class GattCallback(
            val onConnect: () -> Unit,
            val onNotify: (characteristic: BluetoothGattCharacteristic) -> Unit,
            val onDisconnect: () -> Unit
        ) : BluetoothGattCallback() {
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            super.onServicesDiscovered(gatt, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                onConnect()
            } else {
                onDisconnect()
            }
        }
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            when (newState) {
                BluetoothGatt.STATE_CONNECTED -> gatt.discoverServices()
                BluetoothProfile.STATE_DISCONNECTED -> onDisconnect()
            }
        }
        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
                super.onCharacteristicChanged(gatt, characteristic)
                if (characteristic.uuid == CHARACTERISTIC_NOTIFY_STATE) {
                onNotify(characteristic)
                }
            }
        }
    }
//_____________________________________________________________________________________________________



//_____________________________________________________________________________________________________
   @SuppressLint("MissingPermission", "StringFormatInvalid")
   private fun setUiMode(isConnected: Boolean) {
        if (isConnected) {
            bleDevicesFoundList.clear()
            rvDevices?.visibility = View.GONE
            startScan?.visibility = View.GONE
            currentConnexion?.visibility = View.VISIBLE
            currentConnexion?.text = getString(R.string.connected_to, BluetoothLEManager.currentDevice?.name)
            disconnect?.visibility = View.VISIBLE
            toggleLed?.visibility = View.VISIBLE
        } else {
            rvDevices?.visibility = View.VISIBLE
            startScan?.visibility = View.VISIBLE
            ledStatus?.visibility = View.GONE
            currentConnexion?.visibility = View.GONE
            disconnect?.visibility = View.GONE
            toggleLed?.visibility = View.GONE
        }
    }
//_____________________________________________________________________________________________________
    private fun setupRecycler() {
        val rvDevice = findViewById<RecyclerView>(R.id.recycler_view)
        rvDevice.layoutManager = LinearLayoutManager(this)
        rvDevice.adapter = DeviceAdapter(bleDevicesFoundList) { device ->
            BluetoothLEManager.currentDevice = device.device
            connectToCurrentDevice()
        }
    }
//_____________________________________________________________________________________________________

    @SuppressLint("CutPasteId")
    override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_scan_bleactivity)

        setupRecycler()
        setUiMode(false)
        rvDevices = findViewById(R.id.recycler_view)
        startScan = findViewById(R.id.button_lancer)
        currentConnexion = findViewById<TextView>(R.id.currentConnexion)
        disconnect = findViewById(R.id.button_deconnexion)
        toggleLed = findViewById(R.id.button_led)
        ledStatus = findViewById<ImageView>(R.id.ledStatus)
        startScan?.setOnClickListener {
            scanLeDevice()
        }
        disconnect?.setOnClickListener {
            disconnectFromCurrentDevice()
        }
        toggleLed?.setOnClickListener {
            toggleLed()
        }



    }




}