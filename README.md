# OnlineChat
Online chat system implemented in java

1. Compile:   
You can compile by visting the main directory and entering `javac */*.java` into the command line.


2. Run:
You can run the program by first starting the server side of the program. Go into the server directory and enter the command `java ChatServerTester <server_port>` will being the execution of the server portion of the system. Now you can start the client side of the system. Go into the client directory and enter the command `java ChatClientTester <server_ip_address> <server_port>`

3. Authentication:
Hopefully the application can guide you through this part. Use, 'y' or 'n' responses, and give non-blank inputs for username and password prompts.

4. Chat Usage:
You can use the following chat commands:
1) whoelse: shows the names of other connected users
2) wholasthr: shows the names of other users who connected in the last hr
3) message <user> <message>: sends a private message to user
4) broadcast <message>: broadcasts message to all connected users
5) block <user>: block future messages from a user
6) unblock <user>: unblock future messages from a user
7) popular: shows the most and least messaged users
8) takeback <user>: delete undelivered messages sent to another user
9) logout: logout current user
