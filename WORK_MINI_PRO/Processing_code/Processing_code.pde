/*
Application for DIY- Speedometer
can be used with Arduino hardware

code by www.circuitdigest.com
coded on 08-04-2017
*/



//**Import the necessary header files**//
import android.content.Intent;
import android.os.Bundle;
import ketai.net.bluetooth.*;
import ketai.ui.*;
import ketai.net.*;
import android.bluetooth.BluetoothAdapter;
import android.view.KeyEvent;
//__End of imports__//
BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();

PImage needle, speedval, speedcover, neon, gloss,logo;
KetaiBluetooth bt;
int info,speed;
int data, i;
String cninfo = "";
float angle;
boolean sw;



//**To start BT when app is launched**// 
void onCreate(Bundle savedInstanceState) {
 super.onCreate(savedInstanceState);
 bt = new KetaiBluetooth(this);
}
void onActivityResult(int requestCode, int resultCode, Intent data) {
  bt.onActivityResult(requestCode, resultCode, data);
}
//__BT launched__//


//**To select bluetooth device if needed**// (not required for our program
void onKetaiListSelection(KetaiList klist)
{
 String selection = klist.getSelection();
 bt.connectToDeviceByName(selection);
 //dispose of list for now
 klist = null;
}
//__End of selection__//


//** To get data from blue tooth**/
void onBluetoothDataEvent(String who, byte[] data)
{
info = (data[0] & 0xFF) ;
}
//__data received_//


//**To get connection status**//
String getBluetoothInformation()
{
  String btInfo = "Connected to :";

  ArrayList<String> devices = bt.getConnectedDeviceNames();
  for (String device: devices)
  {
    btInfo+= device+"\n";
  }

  return btInfo;
}
//--connection status received_//


//**Settings for the Android Application**//
void settings()
{
 fullScreen(); //make the app for in full screen 
}
//__Settings completed__//


//**Executes only once**// (similar to arduino)
void setup() 
{
  textSize(31);
  bt.start(); //start listening for BT connections
  bt.getPairedDeviceNames();
  bt.connectToDeviceByName("HC-06"); //Connect to our HC-05 bluetooth module
  
  //size(216,384);
  
   //**Load all images from data file**//
  needle = loadImage("needle.png");
  speedval = loadImage("speedval.png");
  speedcover = loadImage("speedcover.png");
  neon = loadImage("neon.png");
  gloss = loadImage("gloss.png");
  logo = loadImage("logo.png");
//End of loading images**//

}


//**Draw function**//
void draw() //The infinite loop
{
 background(#3944FF);
    imageMode(CENTER);
  image(logo, width/2, height/1.04, width, height/12);
  image(speedval, width/2, height/3.8, width/2, width/2);
  image(speedcover, width/2, height/3.8, width/1.2, width/1.2);
  image(neon, width/2, height/3.8, width/1.1, width/1.1);
  image(gloss, width/2, height/3.8, width/1.3, width/1.3);



  display_time();
  speedo();
  textfun();
  getval();
}
///__End of draw__//



void display_time()
{
fill(255);
  int s = second(); 
  int m = minute(); 
  int h = hour(); 
  if (h>12)
  h=h-12;
  textSize(80);
  textAlign(CENTER);
  text(h+":"+m+":"+s, width/2, height/1.5); 
}



//Function Display the text on top of the application**//
void textfun()
{ 
  textSize(30);
  textAlign(CENTER);
  fill(255);
  cninfo = getBluetoothInformation();    //get connection information status
  text(cninfo,width/2,height-height/1.03);
  noFill();
}
//__End of function__//


void speedo()
{ 
  pushMatrix();
  translate(width/2, height/4);
  angle = map(speed, 0, 50, 40, 115);
  rotate(angle*0.1);
  tint(255, 0, 0); 
  image(needle, 0, 0, width/3.5, width/3.5);
  noTint();
  popMatrix();
  textSize(50);
  fill(255,0,0);
  text(speed, width/2, height/2.8);
  println(speed);
  
}

void getval()
{
speed=info;
}