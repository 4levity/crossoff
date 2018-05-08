# crossoff-server LAN web app #

crossoff-server is a self-contained LAN web app to manage a ticket database. 

crossoff-server looks for its database in **./database/** and creates if it does not exist. 
It writes logs to console and to **./log/crossoff.log** .

There is currently no security or access control. The intent is that this would only 
be used on a private LAN, secured with WPA2. Only approved users would have access 
to the network. Note that with access to the network, anyone can add or delete tickets,
mark them scanned, change them, etc.

To build + test + make self contained jar + run server (from crossoff-server folder):

    ./gradlew build
    java -jar build/libs/crossoff-server-1.0.jar 

If the Crossoff android app is configured correctly, it will be able to contact the
server in order to scan tickets.

For user interface, including manual scanning and editing, point your web browser to:

    http://localhost:8080

To add tickets from a Brown Paper Tickets list, browse to admin screen and follow instructions
to use the "wizard".
There is an example file at **sample_files/bpt_complete_list_example.xls**. Note that it is a 
*tab-separated* spreadsheet just like the real thing from BPT; be careful if you edit it.

Or, to add tickets manually without using the BPT import wizard (using Postman, curl etc):

    POST http://localhost:8080/tickets

For an example of the POST body for above:

    GET http://localhost:8080/tickets/example
