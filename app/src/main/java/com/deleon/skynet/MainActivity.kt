package com.deleon.skynet

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import com.deleon.skynet.card.CardHelper
import com.deleon.skynet.card.CardHelper.Companion.asText
import com.deleon.skynet.card.CardManager
import com.deleon.skynet.card.CardModel
import com.deleon.skynet.printer.PrinterConnection
import com.deleon.skynet.printer.Printing
import com.deleon.skynet.printer.PrintingBluetooth
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {
    private val skynet by lazy { application as SkynetApp }
    private val cardManager by lazy { CardManager(skynet.repository) }
    private lateinit var helper: CardHelper
    private var onBluetooth: OnBluetoothGranted? = null
    private val importer = registerForActivityResult(ActivityResultContracts.GetContent()) { uriResult ->
        uriResult?.let { uri ->
            val mCards = mutableListOf<CardModel?>()
            contentResolver.openInputStream(uri)?.bufferedReader()?.forEachLine { mCards.add(CardHelper.createCard(it)) }
            cardManager.addCards(mCards.filterNotNull().toTypedArray(), skynet.doScope) { models -> refreshHelper(models) }
        }
    }
    private val exporter = registerForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uriTarget ->
        uriTarget?.let {
            contentResolver.openOutputStream(it)?.use { stream ->
                stream.write(helper.toString().toByteArray())
                stream.flush()
                stream.close()
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val holder: LinearLayoutCompat = findViewById(R.id.users_list_holder)
        val fab: FloatingActionButton = findViewById(R.id.fab)
        val fab2: FloatingActionButton = findViewById(R.id.fab2)
        helper = CardHelper(this, holder)
        cardManager.getCards(skynet.doScope) { refreshHelper(it) }
        fab.setOnClickListener {
            importer.launch("text/*")
        }
        fab2.setOnClickListener {
            exporter.launch("vouchers-${System.currentTimeMillis()}.csv")
        }
    }

    private fun refreshHelper(cardModels: Array<CardModel>) {
        helper.refresh(
            cards = cardModels,
            onDeleteListener = { deleteCard(it) },
            onPrintListener = { model, server, label -> printCard(model, server, label) },
            onSaveListener = { newPrice, targetProfile -> editCard(newPrice, targetProfile) }
        )
    }
    private fun printCard(model: CardModel, server: String, label: String) {
        val text = model.asText(server, label)
        val btAddress = "86:67:7A:63:4D:9A"
        onBluetoothOk {
            if (!BluetoothAdapter.checkBluetoothAddress(btAddress)) return@onBluetoothOk
            val device = (getSystemService(BLUETOOTH_SERVICE) as BluetoothManager).adapter
                .getRemoteDevice(btAddress)
            val printer = Printing.Printer(
                printerConnection = PrinterConnection(this, device),
                203,
                48f,
                32
            ).addText(text)
            PrintingBluetooth(this, skynet.doScope) {
                if (it) { deleteCard(model) }
            }.print(printer)
        }
    }
    private fun deleteCard(cardModel: CardModel) {
        cardManager.deleteCards(arrayOf(cardModel), skynet.doScope) { refreshHelper(it) }
    }
    private fun editCard(newPrice: Double, targetProfile: String) {
        cardManager.editCards(targetProfile, newPrice, skynet.doScope) { refreshHelper(it) }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            when (requestCode) {
                1,
                2
                    -> checkBluetooth(onBluetooth)
            }
        }
    }

    private fun onBluetoothOk(listener: () -> Unit) {
        checkBluetooth(object : OnBluetoothGranted {
            override fun onGranted() {
                listener()
            }
        })
    }

    private fun checkBluetooth(onBluetoothGranted: OnBluetoothGranted?) {
        onBluetooth = onBluetoothGranted
        if (checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.BLUETOOTH_CONNECT), 1)
        } else if (checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.BLUETOOTH_SCAN), 2)
        } else {
            onBluetooth?.onGranted()
        }
    }

    override fun onDestroy() {
        skynet.close()
        super.onDestroy()
    }

    private interface OnBluetoothGranted {
        fun onGranted()
    }
}