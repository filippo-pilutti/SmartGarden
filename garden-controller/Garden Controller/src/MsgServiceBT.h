#ifndef __MSGSERVICEBT__
#define __MSGSERVICEBT__

#include "Arduino.h"
#include "SoftwareSerial.h"

class MsgBT {
  String content;

public:
  MsgBT(const String& content){
    this->content = content;
  }
  
  String getContent(){
    return content;
  }
};

class MsgServiceBT {
    
public: 
  MsgServiceBT(int rxPin, int txPin);  
  void init();  
  bool isMsgAvailable();
  MsgBT* receiveMsg();
  void sendMsg(MsgBT msg);

private:
  String content;
  MsgBT* availableMsg;
  SoftwareSerial* channel;
  
};

#endif
