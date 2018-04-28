# Crossoff LAN ticket scanning system #

Crossoff (Android barcode scanning app)
crossoff-server (Ticket validation server)

Crossoff will attempt to contact crossoff-server at 

    http://10.0.2.2:8080

Note 10.0.2.2 is the address of host machine localhost for Android Virtual Device.

crossoff-server looks for its database in ./database/ and creates if it does not exist. 
It writes logs to console and to ./log/crossoff.log

To build, test, make self contained jar and run server (from crossoff-server folder):

    ./gradlew build
    java -jar build/libs/crossoff-server-1.0.jar 

To get status of all tickets: 

    GET http://localhost:8080/tickets

To add more tickets:

    POST http://localhsot:8080/tickets

For an example of the POST body for above:

    GET http://localhost:8080/tickets/example


