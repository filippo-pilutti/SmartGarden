#include <Arduino.h>
#include "LampsTask.h"
#include "globals.h"

LampsTask::LampsTask() {
}

void LampsTask::init(int period) {
    Task::init(period);
    l1 = new Led(L1_PIN);
    l2 = new Led(L2_PIN);
    l3 = new LedExt(L3_PIN);
    l4 = new LedExt(L4_PIN);
    state = IDLE;
}

void LampsTask::tick() {

    switch (state) {

        case IDLE:
            if (autoState && lightRange < 5) {
                state = AUTO_OPERATING;
                break;
            } else if (manualState) {
                state = MANUAL_OPERATING;
                break;
            } else {
                break;
            }
        
        case AUTO_OPERATING:
            if (autoState && lightRange >= 5) {
                l1->switchOff();
                l2->switchOff();
                l3->switchOff();
                l4->switchOff();
                state = IDLE;
                break;
            } else if (manualState) {
                state = MANUAL_OPERATING;
                break;
            } else {
                l1->switchOn();
                l2->switchOn();
                l3->setIntensity(map(lightRange, 0, 4, 100, 10));
                l4->setIntensity(map(lightRange, 0, 4, 100, 10));
                break;
            }

        case MANUAL_OPERATING:
            if (!manualState) {
                l1->switchOff();
                l2->switchOff();
                l3->switchOff();
                l4->switchOff();
                state = IDLE;
                break;
            } else {
                manualLed1On ? l1->switchOn() : l1->switchOff();
                manualLed2On ? l2->switchOn() : l2->switchOff();
                l3->setIntensity(map(manualLed3Intensity, 0, 4, 0, 255));
                l4->setIntensity(map(manualLed4Intensity, 0, 4, 0, 255));
                break;
            }
    }
}
