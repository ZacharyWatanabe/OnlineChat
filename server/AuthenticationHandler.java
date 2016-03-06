/**
 * AuthenticationHandler.java
 * Author: Zachary Watanabe-Gastel
 * 
 * Manages user signup, logins, blocking ip addresses after failed
 * login attempts.
 */

import java.io.*;
import java.net.*;
import java.util.*;
public class AuthenticationHandler{
    
    private Map<String,User> chatUsers;
    private Map<String,Long> blockedIpAddrs;
    
    private PrintWriter sender;
	private BufferedReader reciever;
	private Socket socket = null;
    
    //Duration of user
    int BLOCK_TIME = 60000;
    
    public AuthenticationHandler(Map<String,User> chatUsers, Map<String,Long> blockedIpAddrs, 
        Socket socket, PrintWriter sender, BufferedReader reciever){
                
        this.chatUsers = chatUsers;
        this.blockedIpAddrs = blockedIpAddrs;
        this.socket = socket;
        this.sender = sender;
        this.reciever = reciever;
    }
    
    //Helps user choose whether to login or signup
	public String authenticate() throws IOException{
        
        //If the ip of the connection is blocked, fail authentication.
        if(isIpBlocked()) return null;
        
        String[] actions = new String[] {"create a new user", "log in"};
        for(String action : actions){
            
            sender.println("Would you like to " + action + "? (y/n): ");
            String input = reciever.readLine();
            while(!input.equals("y") && !input.equals("n")){
                sender.println("Please use 'y' for yes and 'n' for no. Would you like to " + action + "? (y/n): ");
                input = reciever.readLine();
            }
            if(action.equals("create a new user") && input.equals("y")){
                return createUser();
            }
            else if(action.equals("log in") && input.equals("y")){
                return loginUser();
            }
        }
        return null;
    }    
    
    //Collects a username and password from user, creates a user, and logs them in
    private String createUser() throws IOException{
        //Have client input username and password
        String[] prompts = new String[] {"username","password"};
        String input = "";
        for(String prompt : prompts){
            sender.println("Please enter the "+ prompt+" that you would like to use: ");
            input += reciever.readLine().trim() + " ";
        }
        
        User addedUser = new User(input);
        addedUser.updateLogin(true);
        chatUsers.put(addedUser.getUsername(), addedUser);
        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("user_pass.txt", true)));
        out.println(input);
        out.close();
        return addedUser.getUsername();
    }    
       
    //Manages the login process   
    private String loginUser() throws IOException{
		//You get 3 tries to find a correct user name and password before your ip address is blocked
        String username = "";
        String password = "";
		for (int tries = 0; tries < 3; tries++){
			try{
                sender.println("Username: ");
                username = reciever.readLine();
                sender.println("Password: ");
                password = reciever.readLine();
			}catch(SocketTimeoutException e){
				sender.println("exit");
				break;
			}
            if(isLoginCorrect(username, password)) return username;
            
            handleFailedLogin(tries);
			
		}
        return null;
	}    
    
    
    //Validates the username and password
    private boolean isLoginCorrect(String username, String password){
        //Make sure username exists
        if(chatUsers.get(username) != null){
            User checkUser = chatUsers.get(username);
            //Make sure password is correct
            if(checkUser.getUserPassword().equals(password)){
                //Prevent user concurently accessing same account
                if(checkUser.getLogin()){
                    sender.println("mssg");
                    sender.println("Sorry, user is already logged in.");
                }
                else{
                    sender.println("mssg");
                    sender.println("Welcome to the Chat, you have successfully logged in!");
                    checkUser.updateLogin(true);
                    chatUsers.replace(username, checkUser);
                    return true;
                }
            }
            else{
                    sender.println("mssg");
                    sender.println("Sorry, password is incorrect.");
            }
        }
        else{
            sender.println("mssg");
            sender.println("Sorry, user not found.");
        }
        return false;
    }
    
    //Checks if user's ip address is blocked
    private boolean isIpBlocked(){
         InetAddress client = socket.getInetAddress();
         if(blockedIpAddrs.get(client.getHostAddress()) != null){
             if((System.currentTimeMillis() - blockedIpAddrs.get(client.getHostAddress()))  < BLOCK_TIME){
                 sender.println("mssg");
                 sender.println("Sorry, you are still blocked");
                 return true;
             }
         }
         return false;
    }
    
    //Manages server actions for failed login attempts
    private void handleFailedLogin(int tries) throws IOException{
        if(tries < 2){
            //User gets another try
            sender.println("mssg");
            sender.println("The server was unable to find a match. Please try again:");
        }
        else{
            //User is blocked for 60 seconds
            sender.println("exit");
            sender.println("Login unsuccessful, you have been blocked for 60 seconds.");
                
            //Record ip address and time of failed attempts 
            InetAddress client = socket.getInetAddress();
            blockedIpAddrs.put(client.getHostAddress(), System.currentTimeMillis());
            
            //Close connection to client
            sender.close();
            reciever.close();
            socket.close();
            
        }
    }
    
}





 