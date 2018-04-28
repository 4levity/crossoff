# Crossoff LAN ticket scanning system #

Author: C. Ivan Cooper ivan@4levity.net

This project includes two parts:
* Crossoff (Android barcode scanning app)
* crossoff-server (Java 8 Ticket validation server)

Crossoff app will attempt to contact crossoff-server at this hardcoded address:

    http://10.0.2.2:8080

Note that 10.0.2.2 is the address of the host machine's localhost for an Android Virtual Device.

crossoff-server looks for its database in ./database/ and creates if it does not exist. 
It writes logs to console and to ./log/crossoff.log

To build + test + make self contained jar + run server (from crossoff-server folder):

    ./gradlew build
    java -jar build/libs/crossoff-server-1.0.jar 

To get status of all tickets: 

    GET http://localhost:8080/tickets

To add more tickets:

    POST http://localhost:8080/tickets

For an example of the POST body for above:

    GET http://localhost:8080/tickets/example
