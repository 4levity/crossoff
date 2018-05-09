# Crossoff Android app #

Crossoff app works with crossoff-server to scan barcodes using an Android device camera.

On a successful barcode scan, it will attempt to contact crossoff-server at this hardcoded address:

    http://10.0.2.2:8080

Note that 10.0.2.2 is also the address of the host machine's localhost for an Android Virtual Device.

## Setup for Development, Evaluation, Testing ##

Load the Crossoff project (just file / open and browse to "Crossoff" folder). Then click the 
Run menu, click "Run...", and you will be presented with a "Select Deployment Target" dialog box.

**To use a virtual device:** Click on "Create Virtual Device", select Nexus 5X. Next you select a system 
image, don't select a really old one. On the next screen click "Show Advanced Settings". Then, pull down
the list for Camera Back. It should show a list like "None / VirtualScene / Emulated / webcam0". Pick 
_webcam0_ or whichever option is _not_ VirtualScene or Emulated. You'll have to download some stuff,
then you can select your Nexus 5X under Available Virtual Devices.

**To use a physical device:** If needed, install the manufacturer drivers for your phone or tablet on 
the computer (depends on your device and operating system). On the phone itself, enable Developer Options:
Go to Settings / About phone, then tap Build number seven times and it will say "you are a developer".
Then go to Settings / Developer Options, and enable "USB Debugging" (Or on some Kindle Fire models, 
select Settings > Security and set "Enable ADB" On.) Now, when you plug the device into the computer
with a USB cable, and click "allow" on the device, you should see the device appear in your "Select 
Deployment Target" box. Now you can load Crossoff directly onto the device over USB.

**Hard coded IP address for physical device:** As noted above, Crossoff relies on a hardcoded IP 
address since it is intended to run on a separate network with a specific configuration (see ../README.md). 
But probably the IP address of your test server is not 10.0.2.2 so Crossoff running on a physical 
device won't be able to contact it by default. The easiest way to reconfigure Crossoff app with a 
different server IP address for your test environment is to simply edit the file 
**src/main/java/org/pricelessfestival/crossoff/Scanner.java** 
to change the IP address at the top of the file, then Build / Make Project, and load the modified
app onto your device.