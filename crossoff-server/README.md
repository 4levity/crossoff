# crossoff-server LAN web app #

crossoff-server is a self-contained LAN web app to manage a ticket database. 

crossoff-server looks for its database in ./database/ and creates if it does not exist. 
It writes logs to console and to ./log/crossoff.log

To build + test + make self contained jar + run server (from crossoff-server folder):

    ./gradlew build
    java -jar build/libs/crossoff-server-1.0.jar 

For user interface, point your web browser to:

    http://localhost:8080

To add tickets (using Postman, curl etc):

    POST http://localhost:8080/tickets

For an example of the POST body for above:

    GET http://localhost:8080/tickets/example
