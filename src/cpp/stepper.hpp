#ifndef _STEPPER_HPP_
#define _STEPPER_HPP_

#include <Arduino.h>

class Stepper {
private:
  uint8_t pin;
  uint32_t period;
  uint32_t nextStepTime;
public:
  Stepper(uint8_t newPin);
  Stepper() : Stepper(0xFF) {};
  Stepper& operator=(const Stepper& rhs);
  void setPin(uint8_t newPin);
  void setPeriod(uint32_t newPeriod);
  void run(uint32_t currentTimeMicros);
};

#endif