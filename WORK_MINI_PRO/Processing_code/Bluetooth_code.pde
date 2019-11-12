

//int data, i;
//PImage needle, speedval, speedcover, neon, gloss;
//float angle;


////**Import the necessary header files**//
//import android.content.Intent;
//import android.os.Bundle;
//import ketai.net.bluetooth.*;
//import ketai.ui.*;
//import ketai.net.*;
//import android.bluetooth.BluetoothAdapter;
//import android.view.KeyEvent;
////__End of imports__//


//BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();

//KetaiBluetooth bt;

////**To start BT when app is launched**// 
//void onCreate(Bundle savedInstanceState) {
// super.onCreate(savedInstanceState);
// bt = new KetaiBluetooth(this);
//}
//void onActivityResult(int requestCode, int resultCode, Intent data) {
//  bt.onActivityResult(requestCode, resultCode, data);
//}
////__BT launched__//


////**To select bluetooth device if needed**// (not required for our program
//void onKetaiListSelection(KetaiList klist)
//{
// String selection = klist.getSelection();
// bt.connectToDeviceByName(selection);
// //dispose of list for now
// klist = null;
//}
////__End of selection__//


////** To get data from blue tooth**/
//void onBluetoothDataEvent(String who, byte[] data)
//{
//info = (data[0] & 0xFF) ;

//}
////__data received_//


////**To get connection status**//
//String getBluetoothInformation()
//{
//  String btInfo = "Connected to :";

//  ArrayList<String> devices = bt.getConnectedDeviceNames();
//  for (String device: devices)
//  {
//    btInfo+= device+"\n";
//  }

//  return btInfo;
//}
////--connection status received_//

////**Settings for the Android Application**//
//void settings()
//{
// fullScreen(); //make the app for in full screen 
//}
////__Settings completed__//

//void setup()
//{
//  size(600, 600);
  
//  loadimages();
//  Connect_to_BT();


//}

//void draw()
//{
//  background(255);
//  imageMode(CENTER);
//  image(speedval, width/2, height/4, width/3.5, width/3.5);
//  image(speedcover, width/2, height/4, width/2.7, width/2.7);
//  image(neon, width/2, height/4, width/2, width/2);
//  image(gloss, width/2, height/4, width/2.8, width/2.8);



//  println(data);

//  fill(0);
//  int s = second(); 
//  int m = minute(); 
//  int h = hour(); 
//  textSize(30);
//  textAlign(CENTER);
//  text(h+":"+m+":"+s, width/2, height/2); 


//  speedo();
// // getval();
//}


//void loadimages()
//{
//  //**Load all images from data file**//
//  needle = loadImage("needle.png");
//  speedval = loadImage("speedval.png");
//  speedcover = loadImage("speedcover.png");
//  neon = loadImage("neon.png");
//  gloss = loadImage("gloss.png");
////End of loading images**//
//}

//void Connect_to_BT();
//{
//  bt.start(); //start listening for BT connections
//  bt.getPairedDeviceNames();
//  bt.connectToDeviceByName("HC-05"); //Connect to our HC-05 bluetooth module
//}

//void speedo()
//{ 
//  pushMatrix();
//  translate(width/2, height/4);
//  angle = map(data, 0, 60, 104, 145);
//  rotate(angle*0.1);
//  tint(255, 0, 0); 
//  image(needle, 0, 0, width/3.5, width/3.5);
//  noTint();
//  popMatrix();
//}



//void getval()
//{
//  i = second()/5;
//  if (second()%5==0)
//  {
//    if (i<13)
//    {
//      numbers[i]= data;
//    }
//  }

//  if (i==13)
//    i=0;
//  // println(numbers);
//}