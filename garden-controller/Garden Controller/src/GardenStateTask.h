#include "Task.h"

class GardenStateTask: public Task {
    
    public:
        GardenStateTask();
        void init(int period);
        void tick();
    
    private:
        enum {
            AUTO,
            MANUAL,
            ALARM
        } state;
};