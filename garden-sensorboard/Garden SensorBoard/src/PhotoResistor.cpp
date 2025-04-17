#include "PhotoResistor.h"
#include <Arduino.h>

PhotoResistor::PhotoResistor(int pin) {
    this->pin = pin;
}

double PhotoResistor::detectLight() {
    return analogRead(pin);
}