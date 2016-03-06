/**
 * ChatClient.java
 * Author: Zachary Watanabe-Gastel
 * 
 * This class manages the socket connection to server and the communication proceedures.
 * The communication proceedures are:
 * 1) To recieve a message and respond while accepting incoming mesages
 * 2) To recieve a message and not respond
 * 4) To shutdown the connection, requested by the server
 */
 
import java.io.*;
import java.net.*;
import java.util.*;

public class ChatClient {
	
	private Socket client;                         //Socket connection  
	private PrintWriter sender;                    // Writes to socket
	private BufferedReader receiver;               //  Reads to socket
	BufferedReader in = new BufferedReader(new InputStreamReader(System.in));   //    Keyboad input
    
	//Initializes socket communication
	public ChatClient(String ip, String portNumber) throws NumberFormatException, UnknownHostException, IOException {
		client = new Socket(ip, Integer.parseInt(portNumber));
        sender = new PrintWriter(client.getOutputStream(), true);
		receiver = new BufferedReader(new InputStreamReader(client.getInputStream()));
	}
    
	//Client application
	public void run() throws IOException {
        
        String input = "";
        String get = "";
        
		//Shutdown client if user logs out
		loop: while(!input.equals("logout")){
            get = receiver.readLine();
            switch(get){
                case "mssg":
                    //Protocol 2) To recieve a message and not respond
                    get = receiver.readLine();
				    System.out.println(get);
                    break;
                case "exit":
                    //Protocol 3) To shutdown the connection, requested by the server
                    System.out.println(receiver.readLine());
                    exit();
                    break loop;
                 default:
                    //Protocol 1) To recieve a message and respond while accepting incoming mesages
                    System.out.print(get);
                    input = "abcdef";
                    while(input.equals("abcdef")){
                        if(receiver.ready()){
                            if((get = receiver.readLine()).equals("mssg")){
                                get = receiver.readLine();
                                System.out.println();
                                System.out.println(get);
                            }
                            System.out.print("Command: ");
                        } 
                        if(in.ready()){
                            input = in.readLine();
                        }
                    }
                    sender.println(input);
                    break;
			}
		}
        if(!(get).equals("exit")){
            exit();
        }
	}
    
    private void exit() throws IOException{
        sender.close();
        receiver.close();
        client.close();
    }
}

