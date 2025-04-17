#include "TempSensor.h"
#include <Arduino.h>

TempSensor::TempSensor(int pin) {
    this->pin = pin;
}

double TempSensor::detectTemperature() {
    int value = analogRead(pin);
    // ADC con risoluzione a 12 bit!!!
    double value_in_V = (value / 4095.0);
    double value_in_C = (value_in_V - 0.5) * 100;
    return value_in_C;
}