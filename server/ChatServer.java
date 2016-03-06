/**
 * ChatServer.java
 * Author: Zachary Watanabe-Gastel
 * 
 * This class listens and accepts client connections and delegates managing
 * the client to an independent thread. Data structures intended to be shared 
 * synchronously across threads are also created in this class
 */

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ChatServer {
	
    private ServerSocket listener;         //Listening Socket           
	private ArrayList<Thread> threads;     //List of active threads
    
    //Thread-safe data structures for keeping track of user data and blocked ip connections
    private Map<String,User> chatUsers = new ConcurrentHashMap<String,User>();         
    private Map<String,Long> blockedIpAddrs = new ConcurrentHashMap<String,Long>(); 
    
	//Constructor
	public ChatServer(String portNumber) throws NumberFormatException, IOException {
        //Initialize a listening socket, a lists of users, and a list of threads
        listener = new ServerSocket(Integer.parseInt(portNumber));
        threads  = new ArrayList<Thread>();
        loadUsers("user_pass.txt");
	}

	//Accept new connections and start a thread
	public void run() throws IOException {
		while(true){
			Socket clientConnection = listener.accept();
            Thread thread = new Thread(new ChatThread(clientConnection, chatUsers, blockedIpAddrs));
            thread.start();
			threads.add(thread);
			threadCleanUp();   
		}
	}
    
    //Removes inactive threads
    private void threadCleanUp(){
        for (Iterator<Thread> it = threads.iterator(); it.hasNext();){
            Thread thread = it.next();
            if(!thread.isAlive()){
               it.remove();
            }
        }
    }
    
    //Creates a threadsafe and synchronized list of users to be shared by the threads.
	private void loadUsers(String filename) throws IOException{
		Scanner fileIn = new Scanner(new File(filename));
		while(fileIn.hasNextLine()){
			String userInfo = fileIn.nextLine();
			User added = new User(userInfo);
			chatUsers.put(added.getUsername(), added);
		}
		fileIn.close();
	}
}


		
            
