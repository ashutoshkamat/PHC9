#!/usr/bin/python
# Copyright (c) 2014 Adafruit Industries
# Author: Tony DiCola

# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:

# The above copyright notice and this permission notice shall be included in all
# copies or substantial portions of the Software.

# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
# SOFTWARE.
import sys
import way2sms
import Adafruit_DHT
import firebase_admin
import datetime
from firebase_admin import credentials
from firebase_admin import firestore


cred = credentials.Certificate('/home/pi/DBS4/python-test-4ad25-firebase-adminsdk-d4xiw-15b0681eed.json')
default_app = firebase_admin.initialize_app(cred)

db = firestore.client()


# Parse command line parameters.
sensor_args = { '11': Adafruit_DHT.DHT11,
                '22': Adafruit_DHT.DHT22,
                '2302': Adafruit_DHT.AM2302 }
#if len(sys.argv) == 3 and sys.argv[1] in sensor_args:
sensor = Adafruit_DHT.DHT11
pin = 4
#else:
#    print('Usage: sudo ./Adafruit_DHT.py [11|22|2302] <GPIO pin number>')
#    print('Example: sudo ./Adafruit_DHT.py 2302 4 - Read from an AM2302 connected to GPIO pin #4')
#    sys.exit(1)

# Try to grab a sensor reading.  Use the read_retry method which will retry up
# to 15 times to get a sensor reading (waiting 2 seconds between each retry).
humidity, temperature = Adafruit_DHT.read_retry(sensor, pin)

# Un-comment the line below to convert the temperature to Fahrenheit.
# temperature = temperature * 9/5.0 + 32

# Note that sometimes you won't get a reading and
# the results will be null (because Linux can't
# guarantee the timing of calls to read the sensor).
# If this happens try again!
branch="KOTH001"
if humidity is not None and temperature is not None:
    print('Temp={0:0.1f}*  Humidity={1:0.1f}%'.format(temperature, humidity))
    cur_time = datetime.datetime.now()
    timestamp = str(cur_time.year)+"-"+str(cur_time.month)+"-"+str(cur_time.day)+" "+str(cur_time.hour)+":"+str(cur_time.minute)+":"+str(cur_time.second)
    doc_ref = db.collection(branch+"_temp").document(timestamp)

    doc_ref.set({
        timestamp: unicode(str(temperature))
        })

    print "added to database"
   
   
    if temperature>45:


        q=way2sms.Sms('7875940857','deepblue4')

        if q.send('9404142992','Warning - high temperature '+str(temperature)):
            print 'Sent Successfully'
        else:
            print 'Not sent!'
            
        q.logout()
else:
    print('Failed to get reading. Try again!')
    sys.exit(1)

