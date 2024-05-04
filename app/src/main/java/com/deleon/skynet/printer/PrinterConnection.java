package com.deleon.skynet.printer;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.ParcelUuid;

import com.dantsu.escposprinter.connection.DeviceConnection;
import com.dantsu.escposprinter.exceptions.EscPosConnectionException;

import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

public class PrinterConnection extends DeviceConnection {
    private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    private final BluetoothAdapter adapter;
    private final BluetoothDevice device;
    private BluetoothSocket socket = null;

    public PrinterConnection(Context context, BluetoothDevice device) {
        this((BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE), device);
    }

    public PrinterConnection(BluetoothManager manager, BluetoothDevice device) {
        this(manager.getAdapter(), device);
    }

    public PrinterConnection(BluetoothAdapter adapter, BluetoothDevice device) {
        super();
        this.adapter = adapter;
        this.device = device;
    }

    public BluetoothAdapter getAdapter() {
        return this.adapter;
    }

    public BluetoothDevice getDevice() {
        return this.device;
    }

    @Override
    public boolean isConnected() {
        return this.socket != null && this.socket.isConnected() && super.isConnected();
    }

    @SuppressLint("MissingPermission")
    @Override
    public PrinterConnection connect() throws EscPosConnectionException {
        if (this.isConnected()) {
            return this;
        }
        if (this.adapter == null || !this.adapter.isEnabled()) {
            throw new EscPosConnectionException("Bluetooth adapter unavailable or turned off");
        }
        if (this.device == null) {
            throw new EscPosConnectionException("Bluetooth device disconnected");
        }
        ParcelUuid[] uuids = this.device.getUuids();
        UUID uuid;
        if (uuids != null && uuids.length > 0) {
            if (Arrays.asList(uuids).contains(new ParcelUuid(PrinterConnection.SPP_UUID))) {
                uuid = PrinterConnection.SPP_UUID;
            } else {
                uuid = uuids[0].getUuid();
            }
        } else {
            uuid = PrinterConnection.SPP_UUID;
        }
        try {
            this.socket = this.device.createRfcommSocketToServiceRecord(uuid);
            this.adapter.cancelDiscovery();
            this.socket.connect();
            this.outputStream = this.socket.getOutputStream();
            this.data = new byte[0];
        } catch (IOException e) {
            this.disconnect();
            throw new EscPosConnectionException(e.getMessage());
        }
        return this;
    }

    @Override
    public PrinterConnection disconnect() {
        this.data = new byte[0];
        if (this.outputStream != null) {
            try {
                this.outputStream.close();
            } catch (IOException e) {
                //
            }
            this.outputStream = null;
        }
        if (this.socket != null) {
            try {
                this.socket.close();
            } catch (IOException e) {
                //
            }
            this.socket = null;
        }
        return this;
    }
}
