package com.example.gardenapp;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.example.gardenapp.btlib.BluetoothUtils;
import com.example.gardenapp.btlib.exceptions.BluetoothDeviceNotFound;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private static final String DEVICE_NAME = "isi42";
    private static final String URL = "http://28ab-82-62-78-204.ngrok-free.app/api/status";
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private OutputStream outputStream = null;
    private Handler handler;

    private boolean led1On = false;
    private boolean led2On = false;
    private int led3Count = 0;
    private int led4Count = 0;
    private int irrigationCount = 0;
    private TextView led3TextView, led4TextView, irrigationTextView, statusTextView;
    private Button alarmOffButton;

    private ScheduledExecutorService scheduler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        led3TextView = findViewById(R.id.led3TextView);
        led4TextView = findViewById(R.id.led4TextView);
        irrigationTextView = findViewById(R.id.irrigationTextView);
        statusTextView = findViewById(R.id.statusTextView);
        alarmOffButton = findViewById(R.id.alarmOffButton);

        Button led1Button = findViewById(R.id.led1Button);
        Button led2Button = findViewById(R.id.led2Button);
        Button led3PlusButton = findViewById(R.id.led3PlusButton);
        Button led3MinusButton = findViewById(R.id.led3MinusButton);
        Button led4PlusButton = findViewById(R.id.led4PlusButton);
        Button led4MinusButton = findViewById(R.id.led4MinusButton);
        Button irrigationButton = findViewById(R.id.irrigationButton);
        Button irrigationPlusButton = findViewById(R.id.irrigationPlusButton);
        Button irrigationMinusButton = findViewById(R.id.irrigationMinusButton);
        Button manualControlButton = findViewById(R.id.manualControl2Button);
        Button connectButton = findViewById(R.id.manualControlButton);

        led1Button.setOnClickListener(v -> {
            if (!led1On) {
                led1On = true;
                sendMessage("L1ON\n");
            } else {
                led1On = false;
                sendMessage("L1OFF\n");
            }
        });
        led2Button.setOnClickListener(v -> {
            if (!led2On) {
                led2On = true;
                sendMessage("L2ON\n");
            } else {
                led2On = false;
                sendMessage("L2OFF\n");
            }
        });

        led3PlusButton.setOnClickListener(v -> {
            if (led3Count < 4) {
                led3Count++;
                led3TextView.setText(String.valueOf(led3Count));
                sendMessage("L3"+led3Count + "\n");
            }
        });

        led3MinusButton.setOnClickListener(v -> {
            if (led3Count > 0) {
                led3Count--;
                led3TextView.setText(String.valueOf(led3Count));
                sendMessage("L3"+led3Count + "\n");
            }
        });

        led4PlusButton.setOnClickListener(v -> {
            if (led4Count < 4) {
                led4Count++;
                led4TextView.setText(String.valueOf(led4Count));
                sendMessage("L4"+led4Count+"\n");
            }
        });

        led4MinusButton.setOnClickListener(v -> {
            if (led4Count > 0) {
                led4Count--;
                led4TextView.setText(String.valueOf(led4Count));
                sendMessage("L4"+led4Count+"\n");
            }
        });

        irrigationButton.setOnClickListener(v -> {
            sendMessage("I" + irrigationTextView.getText().toString() + "\n");
            sendMessage("ION\n");
        });

        irrigationPlusButton.setOnClickListener(v -> {
            if (irrigationCount < 4) {
                irrigationCount++;
                irrigationTextView.setText(String.valueOf(irrigationCount));
            }
        });

        irrigationMinusButton.setOnClickListener(v -> {
            if (irrigationCount > 0) {
                irrigationCount--;
                irrigationTextView.setText(String.valueOf(irrigationCount));
            }
        });

        connectButton.setOnClickListener(v -> {
            try {
                connectBluetooth();
            } catch (BluetoothDeviceNotFound e) {
                Toast.makeText(this, "Bluetooth device not found !", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            } finally {
                v.setEnabled(true);
            }
        });

        manualControlButton.setOnClickListener(v -> {
            if (statusTextView.getText().equals("MANUAL")) {
                sendMessage("DISCONNECT\n");
                manualControlButton.setText("Require manual control");
            } else {
                sendMessage("MANUAL-REQUEST\n");
                manualControlButton.setText("Return to AUTO mode");
            }
        });

        alarmOffButton.setOnClickListener(v -> {
            sendMessage("ALARM-OFF\n");
            alarmOffButton.setVisibility(Button.GONE);
        });

        handler = new Handler(Looper.getMainLooper());

        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleWithFixedDelay(this::getSystemStatus, 0, 2, TimeUnit.SECONDS);
    }

    @SuppressLint("MissingPermission")
    private void connectBluetooth() throws BluetoothDeviceNotFound {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            Toast.makeText(this, "Please enable Bluetooth", Toast.LENGTH_SHORT).show();
            return;
        }

        final BluetoothDevice device = BluetoothUtils.getPairedDeviceByName(DEVICE_NAME);
        final UUID uuid = BluetoothUtils.getEmbeddedDeviceDefaultUuid();

        try {
            bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid);
            bluetoothSocket.connect();
            outputStream = bluetoothSocket.getOutputStream();
            Toast.makeText(this, "Connected to device " + device.getName(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "Connection failed", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void sendMessage(String message) {
        if (outputStream != null) {
            try {
                outputStream.write(message.getBytes());
            } catch (IOException e) {
                Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "Not connected to device", Toast.LENGTH_SHORT).show();
        }
    }

    private void getSystemStatus() {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder().url(URL).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                statusTextView.setText("HTTP GET ERROR");
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String responseData = response.body().string();
                     handler.post(() -> {
                       try {
                           JSONObject json = new JSONObject(responseData.substring(responseData.indexOf("{"), responseData.lastIndexOf("}") + 1));
                           String status = json.getString("status");
                           statusTextView.setText(status);
                           if (status.equals("ALARM")) {
                               alarmOffButton.setVisibility(Button.VISIBLE);
                           } else {
                               alarmOffButton.setVisibility(Button.GONE);
                           }
                       } catch (JSONException e) {
                           e.printStackTrace();
                       }
                    });
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (outputStream != null) {
                outputStream.close();
            }
            if (bluetoothSocket != null) {
                bluetoothSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (scheduler != null) {
            scheduler.shutdown();
        }
    }
}
