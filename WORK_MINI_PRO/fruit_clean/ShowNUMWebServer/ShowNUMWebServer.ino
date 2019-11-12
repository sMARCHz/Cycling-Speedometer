/*
 * Copyright (c) 2015, Majenko Technologies
 * All rights reserved.
 * อันนี้ใช้  วิธีสร้างทั้ง Access point และ Web บนตัวเองเลย
 */

#include <ESP8266WiFi.h>
#include <WiFiClient.h>
#include <ESP8266WebServer.h>
#include <ESP8266mDNS.h>

const char *ssid = "FRUIT_ITE";
const char *password = "";

ESP8266WebServer server ( 80 );

const int led = 2;
int HR = 00;
void handleRoot() {
	digitalWrite ( led, 1 );
	char temp[400];
	int sec = millis() / 1000;
	int min = sec / 60;
	int hr = min / 60;

 digitalWrite(led,LOW);
 delay(300);
  if (Serial.available()) {
    HR = Serial.read();
  }
  
 
	snprintf ( temp, 400,

"<html>\
  <head>\
    <meta http-equiv='refresh' content='4'/>\
    <title>ESP8266 Demo</title>\
    <style>\
      body { background-color: #cccccc; font-family: Arial, Helvetica, Sans-Serif; Color: #000088; }\
    </style>\
  </head>\
  <body>\
    <h1> ____DASHBOARD____!</h1>\
    <p>Uptime: %02d:%02d:%02d</p>\
    <h1> TOTAL WEIGHT: %02d </h1>\
   </body>\
</html>",

		hr, min % 60, sec % 60, HR
	);
	server.send ( 200, "text/html", temp );
	digitalWrite ( led, HIGH );
 delay(300);
}

void handleNotFound() {
	digitalWrite ( led, 1 );
	String message = "File Not Found\n\n";
	message += "URI: ";
	message += server.uri();
	message += "\nMethod: ";
	message += ( server.method() == HTTP_GET ) ? "GET" : "POST";
	message += "\nArguments: ";
	message += server.args();
	message += "\n";

	for ( uint8_t i = 0; i < server.args(); i++ ) {
		message += " " + server.argName ( i ) + ": " + server.arg ( i ) + "\n";
	}

	server.send ( 404, "text/plain", message );
	digitalWrite ( led, 0 );
}

void setup ( void ) {
	pinMode ( led, OUTPUT );
	digitalWrite ( led, 0 );
	Serial.begin ( 9600 );

  /* You can remove the password parameter if you want the AP to be open. */
  WiFi.softAP(ssid, password);

  digitalWrite(2, LOW);
  digitalWrite(2,HIGH); //Off LED
 
  IPAddress myIP = WiFi.softAPIP();
  //Serial.print("AP IP address: ");
  //Serial.println(myIP);


	server.on ( "/", handleRoot );
	server.on ( "/inline", []() {
	server.send ( 200, "text/plain", "this works as well" );
	}) ;
	server.onNotFound ( handleNotFound );
	server.begin();
	}

void loop ( void ) {
	server.handleClient();
}


