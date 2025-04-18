package com.example.gardenapp.btlib;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import java.util.Set;
import java.util.UUID;

import com.example.gardenapp.btlib.exceptions.BluetoothDeviceNotFound;

public class BluetoothUtils {

    private static final String DEFAULT_UUID_FOR_EMBEDDED_DEVICES = "00001101-0000-1000-8000-00805F9B34FB";

    public static UUID generateRandomUuid(){
        return UUID.randomUUID();
    }

    public static UUID generateUuidFromString(final String uuid){
        return UUID.fromString(uuid);
    }

    public static UUID getEmbeddedDeviceDefaultUuid(){
        return UUID.fromString(DEFAULT_UUID_FOR_EMBEDDED_DEVICES);
    }

    @SuppressLint("MissingPermission")
    public static Set<BluetoothDevice> getPairedDevices(){
        return BluetoothAdapter.getDefaultAdapter().getBondedDevices();
    }

    @SuppressLint("MissingPermission")
    public static BluetoothDevice getPairedDeviceByName(final String deviceName)
            throws BluetoothDeviceNotFound {

        @SuppressLint("MissingPermission") final Set<BluetoothDevice> pairedList = BluetoothAdapter.getDefaultAdapter()
                .getBondedDevices();

        if(pairedList.size() > 0){
            for(BluetoothDevice device : pairedList){
                if(device.getName().equals(deviceName)){
                    return device;
                }
            }
        }

        throw new BluetoothDeviceNotFound();
    }

    public static BluetoothDevice getPairedDeviceByAddress(final String deviceMacAddress)
            throws BluetoothDeviceNotFound {
        @SuppressLint("MissingPermission") final Set<BluetoothDevice> pairedList = BluetoothAdapter.getDefaultAdapter()
                .getBondedDevices();

        if(pairedList.size() > 0){
            for(BluetoothDevice device : pairedList){
                if(device.getAddress().equals(deviceMacAddress)){
                    return device;
                }
            }
        }

        throw new BluetoothDeviceNotFound();
    }


}
