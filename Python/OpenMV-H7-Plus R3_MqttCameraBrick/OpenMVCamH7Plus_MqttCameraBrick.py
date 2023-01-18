import time, network
import sensor, image, time

from mqtt import MQTTClient

SSID = 'MY_SSID'  # Network SSID
KEY = 'MY_PASSWORD'  # Network key

# Init wlan module and connect to network
print("Trying to connect... (may take a while)...")

sensor.reset()  # Reset and initialize the sensor.
sensor.set_pixformat(sensor.RGB565)  # Set pixel format to RGB565 (or GRAYSCALE)
sensor.set_framesize(sensor.QVGA)  # Set frame size to QVGA (320x240)
sensor.skip_frames(time=2000)  # Wait for settings take effect.
clock = time.clock()  # Create a clock object to track the FPS.

try:
    wlan = network.WINC()
    wlan.connect(SSID, key=KEY, security=wlan.WPA_PSK)
    wlan.ifconfig(('192.168.0.15', '255.255.255.0', '192.168.0.1', '62.2.17.61'))
except OSError:
    pass

if wlan:
    print("WiFi shield connected")
    print("\nFirmware version:", wlan.fw_version())
    print(wlan.ifconfig())
    client = MQTTClient("openmv", "test.mosquitto.org", port=1883)
    client.connect()

    while (True):
        img = sensor.snapshot()
        jpg = img.compressed(quality=35)
        client.publish("bricks/0000-0009/actual", jpg)
        time.sleep_ms(1000)
else:
    print("No WiFi")
