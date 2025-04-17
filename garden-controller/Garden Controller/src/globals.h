#ifndef __GLOBALS__
#define __GLOBALS

#define ALARM_RANGE 5

#define L1_PIN 5
#define L2_PIN 6
#define L3_PIN 3
#define L4_PIN 11

#define SERVO_PIN 2

extern bool autoState;
extern bool manualState;
extern bool alarmState;

extern bool irrigating;

extern int lightRange;
extern int tempRange;

extern bool manualLed1On;
extern bool manualLed2On;
extern int manualLed3Intensity;
extern int manualLed4Intensity;

extern bool manualServoOn;
extern int manualServoSpeed;

extern bool alarmOff;

#endif