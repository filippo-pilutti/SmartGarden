#ifndef __TEMPSENSOR__
#define __TEMPSENSOR__

class TempSensor {

public:
    TempSensor(int pin);
    double detectTemperature();

private:
    int pin;
};

#endif