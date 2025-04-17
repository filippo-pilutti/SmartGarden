#include <Arduino.h>
#include "IrrigationTask.h"
#include "globals.h"

const long irrigationTime = 10000;
const long pauseTime = 10000;

unsigned long tm;
bool servoOperating;

int servoPos;
int delta;

void setDeltaSpeed(int speed);

IrrigationTask::IrrigationTask() {
}

void IrrigationTask::init(int period) {
    Task::init(period);
    servo = new ServoMotorImpl(SERVO_PIN);
    servoPos = 0;
    delta = 1;
    tm = millis();
    servoOperating = false;
    state = IDLE;
}

void IrrigationTask::tick() {
    switch (state) {

        case IDLE:
            if (autoState && lightRange < 2) {
                tm = millis();
                irrigating = true;
                state = AUTO_OPERATING;
                break;
            } else if (manualState) {
                state = MANUAL_OPERATING;
                break;
            } else {
                break;
            }

        case AUTO_OPERATING:
            if (!servoOperating) {
                servoPos = 0;
                setDeltaSpeed(tempRange);
                servo->on();
                servoOperating = true;
            }
            if (millis() - tm <= irrigationTime) {
                servo->setPosition(servoPos);
                servoPos += delta;
                if (servoPos >= 180 || servoPos <= 0) {
                    delta = -delta;
                }
                break;
            } else {
                servo->off();
                irrigating = false;
                servoOperating = false;
                tm = millis();
                state = PAUSE;
                break;
            }

        case MANUAL_OPERATING:
            if (!manualState) {
                state = IDLE;
                break;
            } else if (manualServoOn) {
                if (!servoOperating) {
                    tm = millis();
                    servoPos = 0;
                    setDeltaSpeed(manualServoSpeed);
                    servo->on();
                    servoOperating = true;
                    irrigating = true;
                }
                if (millis() - tm <= irrigationTime) {
                    servo->setPosition(servoPos);
                    servoPos += delta;
                    if (servoPos >= 180 || servoPos <= 0) {
                        delta = -delta;
                    }
                    break;
                } else {
                    servo->off();
                    irrigating = false;
                    servoOperating = false;
                    manualServoOn = false;
                    tm = millis();
                    state = PAUSE;
                    break;
                }
            } else {
                break;
            }

        case PAUSE:
            if (millis() - tm > pauseTime) {
                state = IDLE;
                break;
            } else {
                break;
            }
    }
}

    void setDeltaSpeed(int speed) {
        delta = map(speed, 0, 4, 1, 20);
    }