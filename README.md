# ThingSpeak
Communicate with a ThingSpeak channel sharing your battery life and screen and charger status along with your location once a minuite

README

This app only works on android version 20+ (5.0)

To for this app you must create a ThinkSpeak account and a channel to communicate 

with.  When creating the channel, define three fields.  

field 1 = My Battery
field 2 = Charging
field 3 = Screen on

Copy the API address to communicate with your channel and add it to the define in 

SendBatteryMessage.java so you are communicating with your own channel.

Any other sensor infromation to be sent needs a new field defined within the 

channel to create the graph and can be sent along with the current ones in 

SendBatteryMessage.java
