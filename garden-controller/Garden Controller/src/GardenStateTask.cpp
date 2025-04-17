#include <Arduino.h>
#include "GardenStateTask.h"
#include "globals.h"
#include "MsgService.h"
#include "MsgServiceBT.h"

#define RX_PIN 9
#define TX_PIN 8

MsgServiceBT msgServiceBT(TX_PIN, RX_PIN);

void handleAndUpdateSensorData();
void handleBluetoothManualMessage(String content);


GardenStateTask::GardenStateTask() {}

void GardenStateTask::init(int period) {
    Task::init(period);
    state = AUTO;
    autoState = true;
    MsgService.init();
    msgServiceBT.init();
    MsgService.sendMsg("AUTO");
}

void GardenStateTask::tick() {
    
    // Controllo messaggi sulla seriale per impostare valori letti da ESP
    handleAndUpdateSensorData();

    switch(state) {

        case AUTO:

            // Controllo range di valori per verificare stato di allarme
            if (tempRange >= ALARM_RANGE && !irrigating) {
                autoState = false;
                alarmState = true;
                MsgService.sendMsg("ALARM");
                state = ALARM;
                break;
            }

            // Controllo messaggi bluetooth per verificare richiesta di controllo manuale
            if (msgServiceBT.isMsgAvailable()) {
                MsgBT* msg = msgServiceBT.receiveMsg();
                if (msg->getContent().length() != 0 && msg->getContent().equals("MANUAL-REQUEST")) {
                    autoState = false;
                    manualState = true;
                    MsgService.sendMsg("MANUAL");
                    state = MANUAL;
                    delete msg;
                    break;
                }
                delete msg;
            }

            break;

        case ALARM:

            // Aspetta di ricevere tramite bluetooth lo sblocco del sistema d'allarme
            if (msgServiceBT.isMsgAvailable()) {
                MsgBT* msg = msgServiceBT.receiveMsg();
                if (msg->getContent().length() != 0 && msg->getContent().equals("ALARM-OFF")) {
                    alarmState = false;
                    autoState = true;
                    MsgService.sendMsg("AUTO");
                    state = AUTO;
                    delete msg;
                    break;
                }
                delete msg;
            }

            break;

        case MANUAL:
            
            if (tempRange >= ALARM_RANGE && !irrigating) {
                manualState = false;
                alarmState = true;
                MsgService.sendMsg("ALARM");
                state = ALARM;
                break;
            }

            // Eseguo il controllo dei comandi inviati tramite bluetooth e cambio rispettive variabili globali
            // Controllo se dal bluetooth arriva comando di disconnessione per tornare allo stato AUTO
            if (msgServiceBT.isMsgAvailable()) {
                MsgBT* msg = msgServiceBT.receiveMsg();
                String content = msg->getContent();
                if (content.length() != 0) {
                    if (content.equals("DISCONNECT")) {
                        manualState = false;
                        autoState = true;
                        MsgService.sendMsg("AUTO");
                        state = AUTO;
                        delete msg;
                        break;
                    } else handleBluetoothManualMessage(content);
                }

                delete msg;
                break;
            }

            break;
    }
}

void handleAndUpdateSensorData() {
    if (MsgService.isMsgAvailable()) {
        Msg* msg = MsgService.receiveMsg();
        if (msg->getContent().length() != 0 && msg->getContent()[0] == 'T') {
             tempRange = msg->getContent().substring(1).toInt();
        } else if (msg->getContent().length() != 0 && msg->getContent()[0] == 'L') {
            lightRange = msg->getContent().substring(1).toInt();
        }
        delete msg;
    }
}

void handleBluetoothManualMessage(String content) {
    if (content.substring(0, 2).equals("L1")) {
        if (content.substring(2).equals("ON")) {
            manualLed1On = true;
        } else if (content.substring(2).equals("OFF")) {
            manualLed1On = false;
            }
    } else if (content.substring(0,2).equals("L2")) {
        if (content.substring(2).equals("ON")) {
            manualLed2On = true;
        } else if (content.substring(2).equals("OFF")) {
            manualLed2On = false;
        }
    } else if (content.substring(0,2).equals("L3")) {
        manualLed3Intensity = content.substring(2).toInt();
    } else if (content.substring(0,2).equals("L4")) {
        manualLed4Intensity = content.substring(2).toInt();
    } else if (content.substring(0,1).equals("I")) {
        if (content.substring(1).equals("ON")) {
            manualServoOn = true;
        } else if (content.substring(1).equals("OFF")) {
            manualServoOn = false;
        } else {
            manualServoSpeed = content.substring(1).toInt();
        }
    }
}