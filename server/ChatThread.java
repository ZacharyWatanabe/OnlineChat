/**
 * ChatThread.java
 * Author: Zachary Watanabe-Gastel
 * 
 * This class is called the chat server when a new client connects.
 * the user is authenticated before being allowed to issue commands
 * to the chat system.
 */

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

public class ChatThread implements Runnable {
    
	//TCP components
	private PrintWriter sender;
	private BufferedReader reciever;
	private Socket clientSocket = null;
	
	//Time metrics
	int LAST_HOUR = 3600000;
	int TIME_OUT = 30*60*1000;
	
	Map<String,User> chatUsers;
	Map<String,Long> blockedIpAddrs;
	String curUsername = "";
    
	public ChatThread(Socket clientSocket, Map<String,User> chatUsers, Map<String,Long> blockedIpAddrs) {
		//Thread shared data structures
		this.chatUsers = chatUsers;
		this.blockedIpAddrs = blockedIpAddrs;
       
		//Initialize TCP variables
		this.clientSocket = clientSocket;
		try {
			//Sets readLine method to time out if inactive for set period
			this.clientSocket.setSoTimeout(TIME_OUT);
			sender = new PrintWriter(this.clientSocket.getOutputStream(), true);
			reciever = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e){
            		e.printStackTrace();
        	}
	}

	//Main Thread delegate
	@Override
	public void run(){
		try {
			AuthenticationHandler auth = new AuthenticationHandler(chatUsers, blockedIpAddrs, clientSocket, sender, reciever);
			curUsername = auth.authenticate();          
			clientManager();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//Manages the sending and recieving of commications between server and client
	private void clientManager() throws IOException{
		String command = "";
		boolean stop = false;
		while(curUsername != null && !stop){    //verify login
			try{
				sender.println("Command: ");
				while(!reciever.ready()){
					message_pusher();
				}
				command = reciever.readLine();
 				stop = commandHandler(command);
			}catch(SocketTimeoutException e){
				sender.println("exit");
				logout();
				break;
            		}
		}
	}        
    
	//User commands are handled here
	//@return: boolean value of whether to terminate client connection
	private boolean commandHandler(String command) throws IOException, SocketTimeoutException{
		String[] action = command.split(" ");
		switch(action[0]){
               		case "help": help(); break;
               		case "menu": help(); break;
               		case "whoelse": whoelse(); break;
               		case "wholasthr": wholasthr(); break;
               		case "message": message(command); break;
                	case "broadcast":broadcast(command); break;
                	case "popular": popular(); break;
                	case "takeback": takeback(); break;
                	case "block": block(action[1]); break;
                	case "unblock": unblock(action[1]); break;
                	case "logout": 
                    		logout();
                    		return true;
                	case "refresh":; break;
                	default:
                    		sender.println("mssg");
				sender.println("ERROR: Command Not Recognized");
                    		break;
            	}
            	return false;
	}
    
	//Explains Usage to user
	private void help(){
		String[] menu = {"whoelse: shows the names of other connected users","wholasthr: shows the names of other users who connected in the last hr",
                         	  "message <user> <message>: sends a private message to user","broadcast <message>: broadcasts message to all connected users",
                         	  "block <user>: block future messages from a user","unblock <user>: unblock future messages from a user",
                         	  "popular: shows the most and least messaged users", "takeback <user>: delete undelivered messages sent to another user",
                         	  "logout: logout current user"};
        	sender.println("mssg");
        	sender.println("You can use the following chat commands:");
        	for(int i =1; i <= menu.length; i++){
         		sender.println("mssg");
        		sender.println(i + ") " + menu[i-1]);
        	}
    	}
    
	//Allows user to allow future messages from a user they had previously blocked
    	private void unblock(String userToUnblock){
       		User current = chatUsers.get(curUsername);
		boolean success = current.unblockUser(userToUnblock);
        	if (!success){
             		sender.println("mssg");
             		sender.println("Error: user was not blocked");
        	}
        	chatUsers.replace(curUsername, current);
        	sender.println("mssg");
        	sender.println("You have successfully unblocked " + userToUnblock);
	}
    
    	//Allows user to block future messages from a user of their choice
	private void block(String userToBlock){
       		User current = chatUsers.get(curUsername);
       	 	if (curUsername.equals(userToBlock)){
             		sender.println("mssg");
             		sender.println("Error: You cannot block yourself");
        	}
        	current.blockUser(userToBlock);
        	chatUsers.replace(curUsername, current);
        	sender.println("mssg");
        	sender.println("You have successfully blocked " + userToBlock+ " from sending you messages");
    	}
    
    
	//Deletes all messages that have not been delivered to a client yet
	private void takeback() {
       		for(User checkUser: chatUsers.values()){
             		checkUser.takebackMessages(curUsername);
       		}    
	}

	//Finds the first found user with the most and least messages recieved
	private void popular() {
		int most = 0;
		String mostPop = "";
		String leastPop = "";
		int least = 1000000;
        	for(User checkUser: chatUsers.values()){
            		if(checkUser.messageCount() > most){
				mostPop = checkUser.getUsername();
				most = checkUser.getTotalMessageCount();
			}
			if(checkUser.messageCount() < least){
				leastPop = checkUser.getUsername();
				least = checkUser.getTotalMessageCount();
			}
        	}
		sender.println("mssg");
		sender.println("The first-most popular user (most recieved messages) is: " + mostPop + " with " + most + " messages");
		sender.println("mssg");
		sender.println("The first-least popular user (least recieved messages) is: " + leastPop + " with " + least + " messages" );
	}

	//Takes message queue and sends to user
	private void message_pusher(){
       		User current = chatUsers.get(curUsername);
        	int messageCount = current.messageCount();
        	for(int i = 0; i < messageCount; i++){
            		sender.println("mssg");
            		sender.println(current.pushRecievedMessage(0));
        	}
	}

	//Send a message to all online users
	private void broadcast(String command) {
		String[] parse= command.split(" ");
		User current = chatUsers.get(curUsername);
        	int remove = parse[0].length() + 1;
        	if( remove < command.length()){
          		Message sending = new Message(current.getUsername(),command.substring(remove));
            		for(User checkUser: chatUsers.values()){
                		if(checkUser.getLogin() && !checkUser.getUsername().equals(curUsername)){
                    			checkUser.addMessage(sending);
                    			chatUsers.replace(checkUser.getUsername(),checkUser);
                		}
            		}
        	}    
        	else{
             		sender.println("mssg");
		    	sender.println("There was no message. Please try again.");
        	}  
	}

	//Parse user input and send a message
	private void message(String command) {
		String[] parse= command.split(" ");
		User current = chatUsers.get(curUsername);
        	int remove = parse[0].length() + parse[1].length() + 2;
        	if( remove < command.length()){
            		Message sending = new Message(current.getUsername(),command.substring(remove));
            		for(User checkUser: chatUsers.values()){
                		if(parse[1].equals(checkUser.getUsername())){
                    			checkUser.addMessage(sending);
                    			chatUsers.replace(checkUser.getUsername(),checkUser);
                		}    
            		}
        	}    
        	else{
             		sender.println("mssg");
		     	sender.println("There was no message. Please try again.");
        	}    
	}

	//Checks to see who logged out less than an hour ago
	private void wholasthr() {
		sender.println("mssg");
		sender.println("Users who logged in during the past hour:");
		for(User checkUser: chatUsers.values()){
			if((System.currentTimeMillis() - checkUser.getLoginTime()) < LAST_HOUR && !curUsername.equals(checkUser.getUsername())){
				sender.println("mssg");
				sender.println(checkUser.getUsername());
            		}
        	}	
	}

	//Produces list of users logged on
	private void whoelse() {
		sender.println("mssg");
		sender.println("Users who are currently logged in:");
        	for(User checkUser: chatUsers.values()){
            		if(checkUser.getLogin() && !curUsername.equals(checkUser.getUsername())){
                		sender.println("mssg");
				sender.println(checkUser.getUsername());
            		}
        	}
	}

	//Logout function
	public void logout() throws IOException{
		User current = chatUsers.get(curUsername);
		current.updateLogin(false);
		chatUsers.replace(curUsername, current);
		sender.println("User was successfully logged out");
		sender.close();
		reciever.close();
		clientSocket.close();
	}
}
