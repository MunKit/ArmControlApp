#!/usr/bin/env python
import time
import serial
import paho.mqtt.client as mqtt

servo1 = 1500  #base
servo2 = 1132  #servo2
servo3 = 868   #servo3
servo4 = 1079  #servo4
servo5 = 1500  #yaw it is static
servo6 = 1000  #clamp

ser = serial.Serial(
              
               port='/dev/ttyS0',
               baudrate = 115200,
               parity=serial.PARITY_NONE,
               stopbits=serial.STOPBITS_ONE,
               bytesize=serial.EIGHTBITS,
               timeout=1
           )

def on_connect(client, userdata, rc):
    print("Connected with result code "+str(rc))
    client.subscribe("RoboticArm/message")
    ser.write('\x231P1500T1000\r\n')
    time.sleep(1)
    ser.write('\x232P1132T1000\r\n')
    time.sleep(1)
    ser.write('\x233P868T1000\r\n')
    time.sleep(1)
    ser.write('\x234P1079T1000\r\n')
    time.sleep(1)
    ser.write('\x235P1500T1000\r\n')
    time.sleep(1)
    ser.write('\x236P1000T1000\r\n')

def on_message(client, userdata, msg):
    print(msg.topic+" "+str(msg.payload))
    global servo1
    global servo2
    global servo3
    global servo4
    global servo5
    global servo6
    resmes = str(msg.payload)
    if (resmes=="Servo1cw"):
	servo1 = servo1 + 100
	if (servo1 >= 2500):
            servo1 = 2500
	ser.write('\x231P'+ str(servo1)+'T500\r\n')
	print '\x231P'+ str(servo1)+'T500'
    elif (resmes=="Servo2cw"):
	servo2 = servo2 + 100
	if (servo2 >= 2200):
            servo2 = 2200
	ser.write('\x232P'+ str(servo2)+'T500\r\n')
	print '\x232P'+ str(servo2)+'T500'
    elif (resmes=="Servo3cw"):
	servo3 = servo3 + 100
	if (servo3 >= 2500):
            servo3 = 2500
	ser.write('\x233P'+ str(servo3)+'T500\r\n')
	print '\x233P'+ str(servo3)+'T500'
    elif (resmes=="Servo4cw"):
	servo4 = servo4 + 100
	if (servo4 >= 2000):
            servo4 = 2000
	ser.write('\x234P'+ str(servo4)+'T500\r\n')
	print '\x234P'+ str(servo4)+'T500'
    elif (resmes=="Servo4ccw"):
	servo4 = servo4 - 100
	if (servo4 <= 500):
            servo4 = 500
	ser.write('\x234P'+ str(servo4)+'T500\r\n')
	print '\x234P'+ str(servo4)+'T500'
    elif (resmes=="Servo3ccw"):
	servo3 = servo3 - 100
	if (servo3 <= 500):
            servo3 = 500
	ser.write('\x233P'+ str(servo3)+'T500\r\n')
	print '\x233P'+ str(servo3)+'T500'
    elif (resmes=="Servo2ccw"):
	servo2 = servo2 - 100
	if (servo2 <= 850):
            servo2 = 850
	ser.write('\x232P'+ str(servo2)+'T500\r\n')
	print '\x232P'+ str(servo2)+'T500'
    elif (resmes=="Servo1ccw"):
	servo1 = servo1 - 100
	if (servo1 <= 500):
            servo1 = 500
	ser.write('\x231P'+ str(servo1)+'T500\r\n')
	print '\x231P'+ str(servo1)+'T500'
    elif (resmes=="release"):
        ser.write('\x236P780T500\r\n')
    elif (resmes=="Hold"):
        ser.write('\x236P1500T500\r\n')

client = mqtt.Client()
client.on_connect = on_connect
client.on_message = on_message

client.connect("iot.eclipse.org", 1883, 60)

client.loop_forever()
