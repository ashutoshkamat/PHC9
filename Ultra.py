#Libraries
import RPi.GPIO as GPIO
import time
import way2sms
import firebase_admin
import datetime
from firebase_admin import credentials
from firebase_admin import firestore
#GPIO Mode (BOARD / BCM)

cred = credentials.Certificate('/home/pi/DBS4/python-test-4ad25-firebase-adminsdk-d4xiw-15b0681eed.json')
default_app = firebase_admin.initialize_app(cred)

db = firestore.client()
branch='KOTH001'
GPIO.setmode(GPIO.BCM)
 
#set GPIO Pins
GPIO_TRIGGER = 18
GPIO_ECHO = 24
 
#set GPIO direction (IN / OUT)
GPIO.setup(GPIO_TRIGGER, GPIO.OUT)
GPIO.setup(GPIO_ECHO, GPIO.IN)
GPIO.setup(7,GPIO.IN)
 
def distance():
    # set Trigger to HIGH
    GPIO.output(GPIO_TRIGGER, True)
 
    # set Trigger after 0.01ms to LOW
    time.sleep(0.5)
    GPIO.output(GPIO_TRIGGER, False)
 
    StartTime = time.time()
    StopTime = time.time()
 
    # save StartTime
    while GPIO.input(GPIO_ECHO) == 0:
        StartTime = time.time()
 
    # save time of arrival
    while GPIO.input(GPIO_ECHO) == 1:
        StopTime = time.time()
 
    # time difference between start and arrival
    TimeElapsed = StopTime - StartTime
    # multiply with the sonic speed (34300 cm/s)
    # and divide by 2, because there and back
    distance = (TimeElapsed * 34300) / 2
 
    return distance
 
if __name__ == '__main__':
    try:
        sent=False
        datauploaded = False
        
            
        dist = distance()
            #dist=300
        print ("Measured Distance = %.1f cm" % dist)

        if dist>=8 and sent!=True:
            q=way2sms.Sms('7875940857','deepblue4')

            if q.send('9404142992','Warning - Running out of water. Water level'+str(10-dist)+' cm'):
                print 'Sent Successfully'
                sent=True
            else:
                print 'Not sent!'
                
            q.logout()
                    
        time.sleep(1)
        cur_time = datetime.datetime.now()
        timestamp = str(cur_time.year)+"-"+str(cur_time.month)+"-"+str(cur_time.day)+" "+str(cur_time.hour)+":"+str(cur_time.minute)+":"+str(cur_time.second)
        doc_ref = db.collection(branch+"_waterlevel").document(timestamp)
        doc_ref.set({
             u'value':unicode(str(10-dist))
                   })
        
        print "added to database"
          
        # Reset by pressing CTRL + C
    except KeyboardInterrupt:
        print("Measurement stopped by User")
        GPIO.cleanup()
        
