#include "Task.h"
#include "ServoMotorImpl.h"

class IrrigationTask: public Task {

    public:
        IrrigationTask();
        void init(int period);
        void tick();

    private:
        ServoMotor* servo;
        enum {
            IDLE,
            AUTO_OPERATING,
            MANUAL_OPERATING,
            PAUSE
        } state;
};