//Compatible with the Arduino IDE 1.0
//Library version:1.1
#include <SoftwareSerial.h>
#include <Wire.h> 
#include "LedControl.h"
//#include <LiquidCrystal_I2C.h>

//LiquidCrystal_I2C lcd(0x3F,16,2);  // set the LCD address to 0x27 for a 16 chars and 2 line display

SoftwareSerial Cycle_BT(8, 9);  // RX, TX
int BluetoothData; // the data given from Computer

float timetaken,rpm,dtime;
float wheelR=0;
int v;
//float v;
int V;
int minimumv=0;
int maximumv=0;
volatile byte rotation;
unsigned long pevtime;
int button1 = 3;    //start stop
int button2 = 4;    //menu exit
int button3 = 5;    //input wheelR
int button4 = 6;    //change digit
int buttonState1 = 0;
int buttonState2 = 0;
int buttonState3 = 0;
int buttonState4 = 0;
//We always have to include the library


/*
 Now we need a LedControl to work with.
 ***** These pin numbers will probably not work with your hardware *****
 pin 12 is connected to the DataIn 
 pin 11 is connected to the CLK 
 pin 10 is connected to LOAD 
 We have only a single MAX72XX.
 */
LedControl lc = LedControl(12,11,10,1);

/* we always wait a bit between updates of the display */
unsigned long delaytime=100;

void setup()
{
  pinMode(button1,INPUT_PULLUP);
  pinMode(button2,INPUT);
  pinMode(button3,INPUT);
  pinMode(button4,INPUT);
  Cycle_BT.begin(9600);
  attachInterrupt(0, magnet_detect, FALLING);
  rotation = rpm = pevtime = 0;

    lc.shutdown(0,false);
  /* Set the brightness to a medium values */
  lc.setIntensity(0,11);
  /* and clear the display */
  lc.clearDisplay(0);
  Serial.begin(9600);
}


void loop()
{
  
  int r = 0;
  float R = 0.00;
  
  int digitChange=0;
  //if(buttonState1==HIGH){

    while(digitalRead(button2)==HIGH){
      wheelR = 0;
     
      for(digitChange = 0; (digitChange <=3) && (digitalRead(button2)==HIGH);)
      {
         if(digitalRead(button3) == HIGH)
         {
            if(r<9)
            {
                r+=1;
                R+=1;
            }
            else
            {
                r=0;
                R=0;
            }
         }
       
        Serial.println(digitalRead(button2));
        Serial.print("WheelR : ");
        Serial.println(wheelR);
        displayInput(r,digitChange);
        if(digitalRead(button4) == HIGH||digitalRead(button2)==LOW)
        {
            if(digitChange == 0)wheelR += (R*10);
            else if (digitChange == 1)wheelR += (R);
            else if (digitChange == 2)wheelR += (R/10.00);
            else if (digitChange == 3)wheelR += (R/100.00);
            digitChange++;
            r=0;
            R=0;
        } 
      }
    }
    Serial.println(digitalRead(2));
    Serial.print("WheelR : ");
    Serial.println(wheelR);
  //hallState = digitalRead ( hallPin ) ;      
  // reading from the sensor and storing the state of the hall effect sensor :
  Serial.println(v);
  //int hald = digitalRead(2);
  //Serial.println(hald);
  delay(100);
  if(millis()-dtime>1500) //no magnet found for 1500ms
  {
    rpm= v = 0;
    Cycle_BT.write(v);

  }
  v = (wheelR)*(rpm)*(0.37699);
  if(v>maximumv)
  {
    maximumv = v;
  }
  if(v<minimumv)
  {
    minimumv = v;
  }
  Cycle_BT.write(v);
  scrollDigits(v);
  //writeArduinoOn7Segment();
//}
}

void magnet_detect()
{
  rotation++;
  dtime=millis();
  if(rotation>=2)
  {
    timetaken = millis()-pevtime; //time in millisec for two rotations
    rpm=(1000/timetaken)*60;    //formulae to calculate rpm
    pevtime = millis();
    rotation=0;
    Cycle_BT.write(v);
    Serial.println("Magnet detected....");
  }
}
/*
  This method will scroll all the hexa-decimal
 numbers and letters on the display. You will need at least
 four 7-Segment digits. otherwise it won't really look that good.
 */
void scrollDigits(int v) {
    
    lc.setDigit(0,0,v/1000,false);
    lc.setDigit(0,1,(v%1000)/100,true);
    lc.setDigit(0,2,(v%100)/10,false);
    lc.setDigit(0,3,(v%10),false);
    delay(delaytime);
    
}
void displayInput(int r,int digitChange)
{
  if(digitChange == 1) lc.setDigit(0,digitChange,r,true);
  else lc.setDigit(0,digitChange,r,false);
      delay(delaytime);
      lc.clearDisplay(0);
      
}


