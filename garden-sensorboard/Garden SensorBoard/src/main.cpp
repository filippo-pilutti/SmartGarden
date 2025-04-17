#include <Arduino.h>
#include <ArduinoJson.h>
#include "TempSensor.h"
#include "PhotoResistor.h"
#include <WiFi.h>
#include <HTTPClient.h>

#define LED_PIN 18
#define TMP_PIN 1
#define PHOTO_PIN 4

const char* ssid = "TIM-76243710";
const char* password = "golfclub2018";

const char *serviceURI = "http://28ab-82-62-78-204.ngrok-free.app";

TempSensor* thermo;
PhotoResistor* photo;

void connectToWifi(const char* ssid, const char* password);

int sendData(String address, String name, int value, int range);

String getStatus(String address);

void setup() {
  Serial.begin(115200);
  while (!Serial) {}
  pinMode(LED_PIN, OUTPUT);
  digitalWrite(LED_PIN, HIGH);
  thermo = new TempSensor(TMP_PIN);
  photo = new PhotoResistor(PHOTO_PIN);
  
  connectToWifi(ssid, password);
}

void loop() {
  if (WiFi.status() == WL_CONNECTED) {

    //Controllo stato attuale del sistema
    String status = getStatus(serviceURI);
    Serial.print("System status: ");
    Serial.println(status);
    if (status.equals("ALARM")) {
      digitalWrite(LED_PIN, LOW);
    } else if (status.equals("AUTO") || status.equals("MANUAL")) {
      digitalWrite(LED_PIN, HIGH);
    }

    delay(500);
    
    //Rilevazione e invio dati temperatura
    int c = thermo->detectTemperature();
    int tempLvl = map(c, 15, 40, 0, 4);
    Serial.print("Temperature: ");
    Serial.println(c);
    Serial.println(analogRead(1));
    Serial.print("Tmp range: ");
    Serial.println(tempLvl);
    int codeTemp = sendData(serviceURI, "Temp", c, tempLvl);
    if (codeTemp == 200){
       Serial.println("temp data sent");   
     } else {
       Serial.println(String("error temp data: ") + codeTemp);
     }

     delay(1000);

    //Rilevazione e invio dati luminositá
    int lightVal = photo->detectLight();
    int lightLvl = map(lightVal, 0, 4095, 0, 7);
    Serial.print("Light analog value: ");
    Serial.println(lightVal);
    Serial.print("Light range: ");
    Serial.println(lightLvl);
    int codeLight = sendData(serviceURI, "Light", lightVal, lightLvl);
    if (codeLight == 200){
       Serial.println("light data sent");   
     } else {
       Serial.println(String("error light data: ") + codeLight);
     }

    delay (1000);
  } else {
    Serial.println("WiFi Disconnected... Reconnect.");
    connectToWifi(ssid, password);
  }

}


void connectToWifi(const char* ssid, const char* password) {
  WiFi.begin(ssid, password);
  Serial.println("Connecting");
  while(WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("");
  Serial.print("Connected to WiFi network with IP Address: ");
  Serial.println(WiFi.localIP());
}

int sendData(String address, String name, int value, int range){  
   HTTPClient http;    
   http.begin(address + "/api/data");      
   http.addHeader("Content-Type", "application/json");    
    
   String msg = 
    String("{ \"name\": \"") + name + "\"" + 
    String(", \"value\": ") + String(value) + 
    String(", \"range\": ") + String(range) + " }";
   
   int retCode = http.POST(msg);   
   http.end();  
      
   return retCode;
}

String getStatus(String address) {
  HTTPClient http;
  http.begin(address + "/api/status");
  int httpCode = http.GET();
  String s = "";
  StaticJsonBuffer<200> jsonBuffer;
  if (httpCode == 200) {
    String payload = http.getString();
    char jsonStatus[payload.length() + 1];
    payload.toCharArray(jsonStatus, payload.length() + 1);
    JsonObject& root = jsonBuffer.parseObject(jsonStatus);
    s = root["status"].as<char*>();
  }
  http.end();
  return s;
}