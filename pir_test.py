import RPi.GPIO as GPIO                           #Import GPIO library
import time
import os
import popen2
import commands
import subprocess#Import time library
GPIO.setmode(GPIO.BOARD)                          #Set GPIO pin numbering
pir = 26
out1=""
flag = False#Associate pin 26 to pir
GPIO.setup(pir, GPIO.IN)                          #Set pin as GPIO in 
print("Waiting for sensor to settle")
time.sleep(2)                                     #Waiting 2 seconds for the sensor to initiate
print "Detecting motion"

while GPIO.input(pir):

    if flag == False : 
        
        print("Motion Detected")
        args= ["python3","/home/pi/DBS4/Flow_test.py"]
        p1=subprocess.Popen(args,stdout=subprocess.PIPE)
          
        flag = True
        
        time.sleep(2)                               #D1- Delay to avoid multiple detection
    time.sleep(0.1)
       

GPIO.cleanup();
time.sleep(5)

out1=p1.communicate()
print "Flow rate"
print out1


          
