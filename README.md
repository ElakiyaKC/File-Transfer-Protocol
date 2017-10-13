# File-Transfer-Protocol
Implemented the file transfer protocol to promote reliable and efficient sharing of files between client and serve
Designed multi-threaded server to enforce multiple clients to transmit multiple files concurrently
Developed a user interface to display the list of files which are present on client and server and file transfer progress

User Authentication:
Place the users.txt file in the path: C:\Users\username\ftp before executing the program.

Command Prompt:
Run the Server:
Go to path where the folder is placed.
1.	cd File Transfer Protocol/FTP-Server/src/ftp
2.	javac – classpath  .. Server.java
3.	cd ..
4.	java ftp.Server 20 21
Run the Client:
Go the path where the folder is placed.
1.	cd File Transfer Protocol /FTP-Client/src/gui
2.	javac -classpath .. ClientLogin.java
3.	cd ..
4.	java gui.ClientLogin 

Eclipse or Intellij IDE:
1.Import the FTP-Client and FTP-Server as a new project separately.
2.Run the Server.java in ftp package with arguments 20 21
3.Run the ClientLogin.java in gui package.



The user name and password are given in the users.txt.
Client path: Give the path of the folder from which you want to upload the files.
The server will create a folder for each user in the path C:\User\username\ftp\clientname
 You can check the upload files in this path.

The downloaded files will be present in the client path. The downloaded files will be overwritten if the file is already present in the client path.



