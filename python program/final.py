#!/usr/bin/env python
import time
import serial
import paho.mqtt.client as mqtt
import math

servo1 = 1500  #base
servo2 = 1132  #servo2
servo3 = 868   #servo3
servo4 = 1079  #servo4
servo5 = 1500  #yaw it is static
servo6 = 1000  #clamp

position = [13.1, 0, 3.25, -0.35]
ser = serial.Serial(
              
               port='/dev/ttyS0',
               baudrate = 115200,
               parity=serial.PARITY_NONE,
               stopbits=serial.STOPBITS_ONE,
               bytesize=serial.EIGHTBITS,
               timeout=1
           )
def servocontrol(home):
    global position
    print position
    ang = jointspacecomputation(position)
    ang[0] = mapping(math.degrees(ang[0]),90,-90,833.33,2166.67)
    ang[1] = mapping(math.degrees(ang[1]),90,180,1500,800)
    ang[2] = mapping(math.degrees(ang[2]),90,-90,2166.67,833.33)
    ang[3] = mapping(math.degrees(ang[3]),90,-90,2166.67,833.33)
    ser.write('\x231P'+str(ang[0])+'T560\r\n')
    time.sleep(0.1)
    ser.write('\x232P'+str(ang[1])+'T420\r\n')
    time.sleep(0.1)
    ser.write('\x233P'+str(ang[2])+'T280\r\n')
    time.sleep(0.1)
    ser.write('\x234P'+str(ang[3])+'T140\r\n')
    time.sleep(0.1)
    print "angle "
    print ang
    if (home == True):
        ser.write('\x236P1000T1000\r\n')
        time.sleep(1)

def on_connect(client, userdata, rc):
    print("Connected with result code "+str(rc))
    client.subscribe("RoboticArm/message")
    servocontrol(True)

def mapping(x, input_min,input_max,output_min, output_max):
    return int((x-input_max)*(output_min-output_max)/(input_min-input_max)+output_max);

def jointspacecomputation( axis ):
    delta = [0,0,0,0]
    delta[0] = math.atan2(axis[1],axis[0])
    xp= axis[0] - 15*math.cos(delta[0])*math.cos(axis[3])
    xpp = xp/math.cos(delta[0])
    zp= axis[2] - 15*math.sin(axis[3])
    squarepart = math.sqrt(math.pow(xpp,2) + math.pow(zp,2))
    gamma = math.atan2(-zp/squarepart,-xpp/squarepart)
    delta[1] = gamma - math.acos(-(math.pow(xpp,2) +math.pow(zp,2) + math.pow(10.5,2) - math.pow(9.7,2))/(2*10.5*squarepart))
    delta[2] = math.atan2((zp-10.5*math.sin(delta[1]))/9.7, (xp - 10.5*math.cos(delta[0])*math.cos(delta[1]))/(9.7*math.cos(delta[0]))) - delta[1]
    delta[3] = axis[3] - delta[1] - delta[2]
    for x in range(0,3):
        if (delta[x] > 3.142):
            delta[x]=delta[x]-6.284
        if (delta[x]<-3.142):
            delta[x] = 6.284+delta[x]
    return delta;

def on_message(client, userdata, msg):
    print(msg.topic+" "+str(msg.payload))
    global position
    resmes = str(msg.payload)
    if (resmes=="release"):
        ser.write('\x236P780T500\r\n')
    elif (resmes=="Hold"):
        ser.write('\x236P1500T500\r\n')
    else:
	list1 = msg.payload.split()
    	list2 = []
    	for x in range(0,3):
           list2.append(float(list1[x])/5)
	position[0] = position[0] + list2[0]
	position[1] = position[1] + list2[1]
	position[2] = position[2] + list2[2]
	servocontrol(False)

client = mqtt.Client()
client.on_connect = on_connect
client.on_message = on_message

client.connect("iot.eclipse.org", 1883, 60)

client.loop_forever()
