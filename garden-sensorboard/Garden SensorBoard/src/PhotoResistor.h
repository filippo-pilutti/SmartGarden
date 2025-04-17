#ifndef __PHOTORESISTOR__
#define __PHOTORESISTOR__

class PhotoResistor {

    public:
        PhotoResistor(int pin);
        double detectLight();
    
    private:
        int pin;
};

#endif