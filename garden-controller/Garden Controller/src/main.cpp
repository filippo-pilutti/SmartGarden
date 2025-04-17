#include <Arduino.h>
#include "Scheduler.h"
#include "GardenStateTask.h"
#include "LampsTask.h"
#include "IrrigationTask.h"

Scheduler sched;

bool autoState = false;
bool manualState = false;
bool alarmState = false;

bool irrigating = false;

int lightRange = 3;
int tempRange = 3;

bool manualLed1On = false;
bool manualLed2On = false;
int manualLed3Intensity = 0;
int manualLed4Intensity = 0;

bool manualServoOn = false;
int manualServoSpeed = 0;

bool alarmOff = false;

void setup() {

  sched.init(100);

  Task* t0 = new GardenStateTask();
  t0->init(100);
  t0->setActive(true);
  sched.addTask(t0);

  Task* t1 = new LampsTask();
  t1->init(200);
  t1->setActive(true);
  sched.addTask(t1);

  Task* t2 = new IrrigationTask();
  t2->init(100);
  t2->setActive(true);
  sched.addTask(t2);
}

void loop() {
  sched.schedule();
}