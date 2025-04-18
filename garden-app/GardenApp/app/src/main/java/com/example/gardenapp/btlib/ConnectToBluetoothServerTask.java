package com.example.gardenapp.btlib;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.widget.Toast;

import java.io.IOException;
import java.util.UUID;

public final class ConnectToBluetoothServerTask extends ConnectionTask {

    private BluetoothSocket btSocket = null;

    @SuppressLint("MissingPermission")
    public ConnectToBluetoothServerTask(
            final BluetoothDevice serverBtDevice,
            final UUID uuid,
            final EventListener eventListener
    ){
        try {
            btSocket = serverBtDevice.createRfcommSocketToServiceRecord(uuid);
            this.eventListener = eventListener;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    protected Integer doInBackground(Void... unused) {
        try {
            btSocket.connect();
        } catch (IOException e) {
            e.printStackTrace();
            try {
                btSocket.close( );
            } catch (IOException e1) {
                e1.printStackTrace();
            }

            return CONNECTION_CANCELED;
        }

        connectedChannel = new RealBluetoothChannel(btSocket);

        return CONNECTION_DONE;
    }
}
