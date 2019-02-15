#Works on interrupts
#Output to file at selected intervals
#YF-S201 creates 6 ints/revolution
#Input to Pi through voltage divider
#Input-> 4.7K Ohm res -> Pi pin 13 | 10K Ohm -> Gnd Pin and SR04

import RPi.GPIO as GPIO
import time, sys
import datetime
import way2sms 
import firebase_admin
from firebase_admin import credentials
from firebase_admin import firestore

cred = credentials.Certificate('/home/pi/DBS4/python-test-4ad25-firebase-adminsdk-d4xiw-15b0681eed.json')
default_app = firebase_admin.initialize_app(cred)

db = firestore.client()
branch='KOTH001'
cubicle='1'
f = open('FlowMeterOutput.txt','a')

GPIO.setmode(GPIO.BOARD)
inpt=13
GPIO.setup(inpt,GPIO.IN)
GPIO.setup(26,GPIO.IN)

minutes=0
constant=0.006
time_new=0.0
rpt_int=10

global rate_cnt, tot_cnt
rate_cnt=0
tot_cnt=0

def Pulse_cnt(inpt_pin):
    global rate_cnt, tot_cnt
    rate_cnt +=1
    tot_cnt += 1
    
GPIO.add_event_detect(inpt,GPIO.FALLING,callback=Pulse_cnt, bouncetime=10)

#MAIN
print('Water Flow - Approximate', str(time.asctime(time.localtime(time.time()))))
#rpt_int = int(input('Input desired report interval in seconds '))
rpt_int=5
print('Reports every ', rpt_int, 'seconds')
print('Ctrl C to exit')
f.write('\nWater flow - approximate reports every '+str(rpt_int)+' seconds' + str(time.asctime(time.localtime(time.time()))))

while GPIO.input(26):
    time_new = time.time()+rpt_int #Report interval in secs
    rate_cnt=0
    while time.time() <= time_new: #Reporting loop, x seconds
        try:
            None
            time.sleep(1.0)
            print(GPIO.input(inpt)) #Status indicator
        except KeyboardInterrupt: #Look for exit command
            print('\nExiting..')
            GPIO.cleanup()
            f.close()
            print('Done')
            sys.exit()
            
    minutes+=1
    
    if rpt_int==0:
        LperM=0
        TotLit=0
    else:
        LperM = round(((rate_cnt*constant)/(rpt_int/60.0)),2)
        TotLit = round(tot_cnt*constant,1)
        
    print('\nLitres/min', LperM,'(',rpt_int,'second sample)')
    print('Total Litres', TotLit)
    print('Time (min & clock)',minutes,'\t', time.asctime(time.localtime(time.time())),'\n')
    f.write('\nLiters /min'+ str(LperM))
    f.write('Total Litres'+str(TotLit))
    f.write('Time (min & clock)'+str(minutes)+'\t' +str(time.asctime(time.localtime(time.time()))))
    f.flush()
        
if TotLit<0.1:
    q=way2sms.Sms('7875940857','deepblue4')

    if q.send('9404142992','!!! Warning - Cubical might be unclean !!!'+str(TotLit)+' litres'):
        print('Sent Successfully')
    else:
        print('Not sent!')
                
    q.logout()
elif TotLit>=0.5:
    q=way2sms.Sms('7875940857','deepblue4')

    if q.send('9404142992','!!! Warning - Water Wastage Detected !!!'+str(TotLit)+' litres'):
        print('Sent Successfully')
    else:
        print('Not sent!')
                
    q.logout()

GPIO.cleanup()
#TotLit=54.44
cur_time = datetime.datetime.now()
timestamp = str(cur_time.year)+"-"+str(cur_time.month)+"-"+str(cur_time.day)+" "+str(cur_time.hour)+":"+str(cur_time.minute)+":"+str(cur_time.second)
doc_ref = db.collection(branch+"_waterflow").document(timestamp)
doc_ref.set({
'sensor1': (str(TotLit)),

})

print("added to database")
f.close()
print('DONE')
    
    
    
    
    
    
    
    
    
    
    
    
    

