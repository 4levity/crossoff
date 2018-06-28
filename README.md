# Crossoff LAN ticket scanning system #

Author: C. Ivan Cooper ivan@4levity.net

Crossoff was developed to provide a free system to validate print-at-home, mobile and regular pre-printed 
barcode tickets at an event, without needing internet access. It consists of a server and an Android app 
that uses a phone or tablet camera to scan barcodes. 

Upon scanning a ticket, the user of the app is informed whether the scan was accepted, or if the ticket
was already scanned or voided etc. Also, "door notes" can be associated with a particular ticket and those
will be displayed upon successful scan of that ticket.

It's also possible to use Crossoff to check names/numbers off a central list by hand, without using the 
Android app at all, using just the basic HTML5 user interface provided by the server. But it's not 
really optimized for this use yet.

The UI includes support for easily importing ticket data and barcodes from Brown Paper Tickets, or you can 
generate your own ticket list and use that instead. NOTE: Brown Paper Tickets may change their data format
at any time and then this feature would stop working.

## Project Details ##

There are two subprojects:

* crossoff-server (Java 8 ticket validation server and UI)
* Crossoff (Android barcode scanning app)

crossoff-server is a self-contained LAN web app to manage the ticket list for an event. Crossoff app works 
with crossoff-server to scan barcodes using an Android device camera. See below and also see README.md files 
in each subproject directory for further instructions.

## Example Usage ##

Alice (a programmer) built her own system to collect donations and email tickets to her event. Space is limited and
demand is high, so she needs to make sure that every ticket presented at the door is valid. But checking 
everyone off a list is too slow. She knows how to generate random barcodes and email them to her 
ticketholders, and she needs a _free_ system that lets her have multiple barcode scanners running at once.

Bob (not a programmer) is selling tickets to his event using Brown Paper Tickets. He wants to allow electronic 
ticketing so that users can print their ticket at home and send tickets to other people electronically. However, 
he cannot use the free Brown Paper Tickets app, because there is no reliable Internet service available at the
location where tickets will be scanned.

In both of these cases, Crossoff might be a usable solution, and they could set up their hardware the 
same way (see Setup section below).

Before the event starts, Alice would need to generate a JSON file using the Crossoff format. To see an 
example, start the server and browse to _http://localhost:8080/tickets/example_. It just needs to have
a few fields - the barcode for each attendee, their name (optional) and a ticket description and type.
Then she'd start up crossoff-server and use curl or Postman to load the tickets via the simple API.

Since Bob is using Brown Paper Tickets to sell his tickets and generate barcodes, he doesn't have to deal 
with JSON and APIs. He just logs into the Brown Paper Tickets website right before the event, and downloads 
the "Complete List" for the event. The UI on crossoff-server allows him to quickly import this file and 
he is ready to start scanning.

### Last Minute Sales ###

If Alice wants to allow last minute ticket sales, she could use the crossoff-server API to add more 
tickets as they are sold. Bob could theoretically do that too, but to keep things simple, he might want 
to stop new sales on Brown Paper Tickets, then download his final list of attendees and load them into 
the database.

## Setup for Testing and Evaluation ##

You can test the system end-to-end on a laptop. Using a physical Android device is optional. First, get
the server running from the instructions in **crossoff-server/README.md** . Next, follow the instructions
in **Crossoff/README.md** to get the Android app running on a real or virtual Android device.

Note that if you are using Android Virtual Device, the easiest way to make everything work is to run the
virtual device on the same computer that crossoff-server is running on.

## Setup in the Wild ##

A basic setup at an event would look like the following. Once it is set up it should "just work" due to 
the magic of hard-coded IP addresses and a complete lack of built-in security - the only security is 
provided by WPA2, and by not connecting anything else to your ticket validation network.

* One WiFi router configured as follows:
  * Router IP address 10.0.2.1
  * LAN address 10.0.2.0/24 (i.e. netmask 255.255.255.0)
  * DHCP range 10.0.2.100 - 10.0.2.200 (or whatever, but not including 10.0.2.2)
  * WPA2-PSK (WPA2 Personal) encryption for the WiFi, using a secure+secret password
  * No Internet access (recommended)
* One laptop or PC or Raspberry Pi or similar (also maybe a spare) configured as follows:
  * Any operating system, Java 8 installed
  * Connected to the router (WiFi or Ethernet) and set up with **static ip address 10.0.2.2**
  * crossoff-server installed and running (optionally you could set it to start automatically on boot up)
  * tickets loaded into crossoff-server database
* One or more phones/tablets
  * Android KitKat or later
  * Connected to the WiFi
  * Running the Crossoff app

Or ... you could use a Raspberry Pi Zero W with `hostapd` installed as both your server and
the WiFi access point, and run the entire service from a small USB battery. 

## Risks ##

This project has no warranty, express or implied, yadda yadda. Use it entirely at your own risk!
And I'm pretty sure I *just* said there's no security of any kind except for the secure network 
you're expected to use it on.

Assuming you tested everything out in advance, probably the biggest disaster that you 
could feasibly experience while using Crossoff is the loss of your ticket server or database
during the event. If you cannot immediately recover the data and/or get the server working again, you will
lose information on what tickets have been scanned in, and maybe lose your ticket list. You might want to
have a printout and maybe a second laptop with a second copy of the database.
