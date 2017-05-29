import paho.mqtt.client as mqtt
import RPi.GPIO as GPIO
from time import sleep
GPIO.setwarnings(False)
GPIO.setmode(GPIO.BCM)
var = 1
GPIO.setup(6, GPIO.OUT)
GPIO.setup(13, GPIO.OUT)
GPIO.setup(19, GPIO.OUT)
GPIO.setup(26, GPIO.OUT)
GPIO.output(6, GPIO.LOW)
GPIO.output(26, GPIO.LOW)
GPIO.output(19, GPIO.LOW)
GPIO.output(13, GPIO.LOW)
def on_connect(client, userdata, rc):
    print("Connected with result code "+str(rc))
    # Subscribing in on_connect() means that if we lose the connection and
    # reconnect then subscriptions will be renewed.
    #client.subscribe("mychannel/andriod")example/Android/Publish/myTopic
    client.subscribe("RoboticArm/message")
# The callback for when a PUBLISH message is received from the server.
def on_message(client, userdata, msg):
    print(msg.topic+" "+str(msg.payload))
    resmes = str(msg.payload)
    if (resmes=="front"):
	GPIO.output(6, GPIO.HIGH)
	GPIO.output(26, GPIO.LOW)
        GPIO.output(13, GPIO.LOW)
	GPIO.output(19, GPIO.LOW)
    elif (resmes=="back"):
	GPIO.output(26, GPIO.HIGH)
	GPIO.output(13, GPIO.LOW)
	GPIO.output(19, GPIO.LOW)
	GPIO.output(6, GPIO.LOW)
    elif (resmes=="left"):
	GPIO.output(13, GPIO.HIGH)
	GPIO.output(19, GPIO.LOW)
	GPIO.output(6, GPIO.LOW)
	GPIO.output(26, GPIO.LOW)
    elif (resmes=="right"):
	GPIO.output(19, GPIO.HIGH)
	GPIO.output(26, GPIO.LOW)
	GPIO.output(13, GPIO.LOW)
	GPIO.output(6, GPIO.LOW)

client = mqtt.Client()
client.on_connect = on_connect
client.on_message = on_message

client.connect("iot.eclipse.org", 1883, 60)

# Blocking call that processes network traffic, dispatches callbacks and
# handles reconnecting.
# Other loop*() functions are available that give a threaded interface and a
# manual interface.
client.loop_forever()
