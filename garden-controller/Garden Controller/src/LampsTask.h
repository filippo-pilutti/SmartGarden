#include "Task.h"
#include "Led.h"
#include "LedExt.h"

class LampsTask: public Task {

    public:
        LampsTask();
        void init(int period);
        void tick();

    private:
        Led* l1;
        Led* l2;
        LedExt* l3;
        LedExt* l4;
        enum {
            IDLE,
            AUTO_OPERATING,
            MANUAL_OPERATING
        } state;
};