# crossoff-server LAN web app #

crossoff-server is a self-contained LAN web app to manage a ticket database. Requires
Java 17.

crossoff-server looks for its database in **./database/** and creates if it does not exist. 
It writes logs to console and to **./log/crossoff.log** .

There is currently no security or access control. The intent is that this would only 
be used on a private LAN, secured with WPA2. Only approved users would have access 
to the network. Note that with access to the network, anyone can add or delete tickets,
mark them scanned, change them, etc.

To build + test + run server locally (from crossoff-server folder):

    ./gradlew build
    ./gradlew run

If the Crossoff android app is configured correctly, it will be able to contact the
server in order to scan tickets.

For user interface, including manual scanning and editing, point your web browser to:

    http://localhost:8080

To add tickets from a Secret Party Guest List export CSV or Brown Paper Tickets list, browse to admin screen and follow instructions
to use the "wizard".

There is an example BPT file at **sample_files/bpt_complete_list_example.xls**. Note that it is a 
*tab-separated* spreadsheet just like the real thing from BPT; be careful if you edit it.

The Secret Party CSV importer can be used for any CSV list of tickets as long as there is a header row and it includes columns named
ticket_code, first_name, last_name, and product. The columns can be in any order and there can be extra columns as well.

Or, to add tickets manually without using either the BPT or SP import wizards (using Postman, curl etc):

    POST http://localhost:8080/tickets

For an example of the POST body for above:

    GET http://localhost:8080/tickets/example
 
To install the server on another device, use the tar or zip file in `build/distributions`.
