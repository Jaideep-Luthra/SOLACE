#include <WiFi.h>
#include <HTTPClient.h>
#include <WiFiClient.h>
#include <Arduino_JSON.h>
#include <WiFiClientSecure.h>

#define SSID "ADMIN12345"
#define PASS "ADMIN12345"
#define LOCK_PIN 15
#define IR_PIN 16

HTTPClient http;
WiFiClient wifiClient;
void setup() {
  // Initialize serial communication
  Serial.begin(115200);
  pinMode(LOCK_PIN, OUTPUT);
  pinMode(IR_PIN, INPUT_PULLUP);
  initWiFi();
  delay(2000);

}
int lockStatus = 0;
void loop() {
  sendDataToServer(digitalRead(IR_PIN));
  getFromServer();
  digitalWrite(LOCK_PIN, lockStatus);
  delay(5000);
}
void sendDataToServer(int irStatus) {
  if (WiFi.status() == WL_CONNECTED) {
    String url =   "https://codingprojects.help/solace/bikeStatus.php?bike_id=1&is_lock=" + String(irStatus);
    WiFiClientSecure client;
    client.setInsecure();  // Skip SSL certificate verification (not recommended for production)


    HTTPClient https;
    https.begin(client, url);  // Specify the URL and client
    int code = https.GET();
    if (code == 200) {
      String payload = https.getString();
      https.end();  // Close the connection
      Serial.println(payload);
    } else {
      Serial.print("HTTP GET request failed with code: ");
      Serial.println(code);
      https.end();  // Close the connection
    }
  } else {
    initWiFi();
  }
}
void getFromServer() {
  if (WiFi.status() == WL_CONNECTED) {
    String url = "https://codingprojects.help/solace/bikeStatus.php?bike_id=1";
    WiFiClientSecure client;
    client.setInsecure();  // Skip SSL certificate verification (not recommended for production)


    HTTPClient https;
    https.begin(client, url);  // Specify the URL and client
    int code = https.GET();
    if (code == 200) {
      String payload = https.getString();
      https.end();  // Close the connection
      // Parse JSON response
      JSONVar jsonObject = JSON.parse(payload);

      if (JSON.typeof(jsonObject) == "undefined") {
        Serial.println("Parsing input failed!");
        return;
      }
      lockStatus = getConvertedFieldData(jsonObject, "bike_status");

    } else {
      Serial.print("HTTP GET request failed with code: ");
      Serial.println(code);
      https.end();  // Close the connection
    }
  } else {
    initWiFi();
  }
}
int getConvertedFieldData(JSONVar jsonObject, String field) {
  // Ensure json_object has the field before copying
  if (jsonObject.hasOwnProperty(field)) {
    String fieldValue = (const char*)jsonObject[field];
    // Convert string to integer
    return fieldValue.toInt();
  }
  Serial.print("'");
  Serial.print(field);
  Serial.println("' not found in the JSON response.");
  return -1;
}

void initWiFi() {
  WiFi.mode(WIFI_STA);
  WiFi.begin(SSID, PASS);
  unsigned long s_time = millis();
  Serial.println("Connecting to WiFi ..");
  while (WiFi.status() != WL_CONNECTED) {
    Serial.print('.');
    if (millis() - s_time > 5000)
      break;
    delay(500);
  }
  Serial.print("WiFi Connected ");
  Serial.println(WiFi.localIP());
}
