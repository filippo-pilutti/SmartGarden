#include <Arduino.h>
#include "ServoMotorImpl.h"

#define MIN 350
#define MAX 2800

ServoMotorImpl::ServoMotorImpl(int pin) {
    this->pin = pin;
}

void ServoMotorImpl::on() {
    if (!motor.attached()) {
        motor.attach(pin);
    }
}

void ServoMotorImpl::setPosition(int angle) {
  // 350 -> 0, 2800 -> 180 
  // 350 + angle*(2250-750)/180
  float coeff = (2800.0-350.0)/180;
  motor.write(350 + angle*coeff);
}

void ServoMotorImpl::off() {
    if (motor.attached()) {
        motor.detach();
    }
}