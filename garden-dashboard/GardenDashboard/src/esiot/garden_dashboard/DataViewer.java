package esiot.garden_dashboard;

import io.vertx.core.Vertx;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;

import javax.swing.*;
import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;

public class DataViewer extends JFrame {

    private static final long serialVersionUID = -3791699112956001264L;
    
    private JTextField tempValueField;
    private JTextField tempRangeField;
    private JTextField lightValueField;
    private JTextField lightRangeField;
    private JTextField statusField;
    private Vertx vertx;
    private WebClient client;
    private Timer timer;

    public DataViewer() {
        // Configurazione della finestra principale
        setTitle("Garden Dashboard");
        setSize(400, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Creazione dei pannelli per visualizzare i dati
        JPanel dataPanel = new JPanel(new GridLayout(7, 2));
        
        dataPanel.add(new JLabel("Temperature:"));
        dataPanel.add(new JLabel(""));

        dataPanel.add(new JLabel("Value:"));
        tempValueField = new JTextField();
        tempValueField.setEditable(false);
        dataPanel.add(tempValueField);

        dataPanel.add(new JLabel("Range:"));
        tempRangeField = new JTextField();
        tempRangeField.setEditable(false);
        dataPanel.add(tempRangeField);

        dataPanel.add(new JLabel("Light:"));
        dataPanel.add(new JLabel(""));

        dataPanel.add(new JLabel("Value:"));
        lightValueField = new JTextField();
        lightValueField.setEditable(false);
        dataPanel.add(lightValueField);

        dataPanel.add(new JLabel("Range:"));
        lightRangeField = new JTextField();
        lightRangeField.setEditable(false);
        dataPanel.add(lightRangeField);

        dataPanel.add(new JLabel("Status:"));
        statusField = new JTextField("AUTO");
        statusField.setEditable(false);
        dataPanel.add(statusField);

        add(dataPanel, BorderLayout.NORTH);

        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);

        add(scrollPane, BorderLayout.CENTER);

        // Inizializzazione di Vert.x e del client WebClient
        vertx = Vertx.vertx();
        client = WebClient.create(vertx);

        // Inizializzazione del timer per aggiornare i dati ogni 2 secondi
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                fetchData(textArea);
                fetchStatus();
            }
        }, 0, 2000); // Aggiorna ogni 2 secondi
    }

    private void fetchData(JTextArea textArea) {
        // URL del server HTTP per i dati
        String url = "http://localhost:8080/api/data";

        client.getAbs(url).send(ar -> {
            if (ar.succeeded()) {
            	
                HttpResponse<Buffer> response = ar.result();
                // Parsing della risposta JSON
                JsonArray jsonArray = response.bodyAsJsonArray();
                StringBuilder displayData = new StringBuilder();
                boolean tempUpdated = false;
                boolean lightUpdated = false;

                for (int i = 0; i < jsonArray.size(); i++) {
                    JsonObject obj = jsonArray.getJsonObject(i);
                    String name = obj.getString("name");
                    int value = obj.getInteger("value");
                    int range = obj.getInteger("range");

                    if (!tempUpdated && "Temp".equals(name)) {
                        String tempValue = String.valueOf(value);
                        String tempRange = String.valueOf(range);
                        SwingUtilities.invokeLater(() -> {
                            tempValueField.setText(tempValue);
                            tempRangeField.setText(tempRange);
                        });
                        tempUpdated = true;
                    } else if (!lightUpdated && "Light".equals(name)) {
                        String lightValue = String.valueOf(value);
                        String lightRange = String.valueOf(range);
                        SwingUtilities.invokeLater(() -> {
                            lightValueField.setText(lightValue);
                            lightRangeField.setText(lightRange);
                        });
                        lightUpdated = true;
                    }

                    displayData.append("Name: ").append(name)
                               .append(", Value: ").append(value)
                               .append(", Range: ").append(range).append("\n");
                }

                // Aggiorna i dati sull'interfaccia
                SwingUtilities.invokeLater(() -> textArea.setText(displayData.toString()));
            } else {
                SwingUtilities.invokeLater(() -> textArea.setText("GET request failed: " + ar.cause().getMessage()));
            }
        });
    }

    private void fetchStatus() {
        // URL del server HTTP per lo stato
        String url = "http://localhost:8080/api/status";

        client.getAbs(url).send(ar -> {
            if (ar.succeeded()) {

                HttpResponse<Buffer> response = ar.result();
                // Parsing della risposta JSON
                JsonObject jsonObject = response.bodyAsJsonObject();
                String status = jsonObject.getString("status");

                // Aggiorna il campo di stato
                SwingUtilities.invokeLater(() -> statusField.setText(status));
            } else {
                // Gestione dell'errore
                SwingUtilities.invokeLater(() -> statusField.setText("STATUS request failed: " + ar.cause().getMessage()));
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            DataViewer viewer = new DataViewer();
            viewer.setVisible(true);
        });
    }
}
