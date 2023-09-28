#include "stepper.hpp"
#include <Arduino.h>

Stepper::Stepper(uint8_t newPin) {
  pin = newPin;
  pinMode(pin, OUTPUT);
  nextStepTime = 0xFFFFFFFF;
  period = 0xFFFFFFFF;
}

Stepper & Stepper::operator=(const Stepper & rhs) {
  pin = rhs.pin;
  pinMode(pin, OUTPUT);
  nextStepTime = rhs.nextStepTime;
  period = rhs.period;

  return *this;
}

void Stepper::setPin(uint8_t newPin) {
  pin = newPin;
  pinMode(newPin, OUTPUT);
}

void Stepper::setPeriod(uint32_t newPeriod) {
  if (newPeriod == 0) {
    period = 0xFFFFFFFF;
    nextStepTime = 0xFFFFFFFF;      
  }
  else {
    period = newPeriod;
    nextStepTime = micros() + newPeriod;
  }
    
}

void Stepper::run(uint32_t currentTimeMicros) {
  if (currentTimeMicros >= nextStepTime) {
    digitalWrite(pin, HIGH);
    digitalWrite(pin, LOW);
    nextStepTime += period;
  }
}
