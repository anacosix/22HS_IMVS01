#include <SPI.h>
#include <Wire.h>
#include <Adafruit_GFX.h>
#include <Adafruit_SH110X.h>
#include <ESP8266WiFi.h>
#include <Adafruit_MQTT.h>
#include <Adafruit_MQTT_Client.h>

#define MQTT_CONN_KEEPALIVE 30

#define WLAN_SSID       "MY_SSID"
#define WLAN_PASS       "MY_PASSWORD"

#define AIO_SERVER      "test.mosquitto.org"
#define AIO_SERVERPORT  1883

const char *topicStr = "bricks/0000-0010/target";

WiFiClient client;
Adafruit_MQTT_Client mqtt(&client, AIO_SERVER, AIO_SERVERPORT);
Adafruit_MQTT_Subscribe topic = Adafruit_MQTT_Subscribe(&mqtt, topicStr);

Adafruit_SH1107 display = Adafruit_SH1107(128, 128, &Wire);

#define img_width 128
#define img_height 128

unsigned char img_bytes[128*128];

void MQTT_connect();

void setup(void) {
  Serial.begin(115200);
  delay(250);

  display.begin(0x3C, true); 
 
  display.display();
  delay(2000);
  display.clearDisplay();
  
  Serial.println();
  Serial.print("Connecting to ");
  Serial.println(WLAN_SSID);

  WiFi.begin(WLAN_SSID, WLAN_PASS);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println();

  Serial.println("WiFi connected");
  Serial.println("IP address: "); Serial.println(WiFi.localIP());

  topic.setCallback(handleMessage);
  mqtt.subscribe(&topic);
}
 
void loop(void) {
  MQTT_connect();
  if (mqtt.connected()) {
    Serial.println("Connected (still)");
    mqtt.processSubscriptionPacket(&topic);
    delay(1000);
    if (!mqtt.ping()) {
      mqtt.disconnect();  
    }
  } else {
    int result = mqtt.connect();
    if (result != 0) {
      Serial.println(mqtt.connectErrorString(result));
      delay(3000);
    }
  }
}


void MQTT_connect() {
  int8_t ret;

  if (mqtt.connected()) {
    return;
  }

  Serial.print("Connecting to MQTT... ");

  uint8_t retries = 3;
  while ((ret = mqtt.connect()) != 0) { 
       Serial.println(mqtt.connectErrorString(ret));
       Serial.println("Retrying MQTT connection in 5 seconds...");
       mqtt.disconnect();
       delay(5000);
       retries--;
       if (retries == 0) {
         while (1);
       }
  }
  Serial.println("MQTT Connected!");
}

void handleMessage(char *data, uint16_t len) {
  byte byteArr[len];
  for(int i = 0; i < len; i++){
    byteArr[i] = (byte)data[i];
  }
  drawbitmap(byteArr,len);
}

void drawbitmap(byte data[],int len) {
  display.clearDisplay();

  display.drawRamBitmap(
    (display.width()  - img_width ) / 2,
    (display.height() - img_height) / 2,
    img_height, img_width, 1,
    data, len);
  display.display();
  delay(1000);
}
