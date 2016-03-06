/* User.java
 * Author: Zachary Watanabe-Gastel
 *
 * This User class stores data for user authentication, messages, and blocked users.
 * Essentially, this class is a model for user specific data on the server side of the program.
 */
 
import java.util.*;

public class User {
    
    //User data
	private String userName;
	private String password;
    
    //Login state and last login time
	private boolean login = false;
	private long loginTime = 0;
    
    //Queue of messages and list of other users who have been blocked by current user.
	private ArrayList<Message> messages = new ArrayList<Message>();
	private ArrayList<String> blocked = new ArrayList<String>();
	
    private int totalMessages = 0;
    
	//Constructor Parses user authentication data
	public User(String userInfo) {
		String[] info = userInfo.split(" ");
		this.userName = info[0];
		this.password = info[1];
	}

	//Access user authentication info
	public String getUsername() {
		return userName;
	}
	public String getUserPassword() {
		return password;
	}
	
	//Login accessor and modifier methods 
	public void updateLogin(boolean current){
	    loginTime = System.currentTimeMillis();
		login = current;
	}
    
	public boolean getLogin(){
		return login;
	}
    
	public long getLoginTime(){
		return loginTime;
	}

	//Recieves messages and adds them to a message queue
	public void addMessage(Message recieved){
		boolean found = false; 
		for(int i = 0; !found && i < blocked.size(); i++){
			if(blocked.get(i).equals(recieved.sender)){
				found = true;
			}
		}
		if(!found){
			messages.add(recieved);
		}
        totalMessages ++;
	}
    
    //Deletes messages sent by user from other user's inboxes
    public void takebackMessages(String sender){
        for(Iterator<Message> it = messages.iterator(); it.hasNext();){
            Message mssg = it.next();
            if(sender.equals(mssg.sender)) it.remove();
        }
    }
    
    //Counts messages
    public int messageCount(){
        return messages.size();
    }
    
    public int getTotalMessageCount(){
        return totalMessages;
    }    
    
    //Delivers message queue to client.
	public String pushRecievedMessage(int index){
		Message output = null;
		if(index == 0) {
			int size = messages.size()-1;
			output = messages.get(size);
			messages.remove(index);
		}
		else {
			output = messages.get(index);
			messages.remove(index);
		}	
		
		return (output.sender + ": " + output.message);
	}
	
	//Manages blocking other users from messaging this client
	public void blockUser(String userName){
		boolean found = false;
		for(int i = 0; !found && i < blocked.size(); i++){
			if(blocked.get(i).equals(userName)){
                found = true;
            }	
		}
		if(!found){
			blocked.add(userName); 
        }    
	}
    
    //Manages unblocking other users from messaging this client
    //@return if user was unblockable
	public boolean unblockUser(String userName){
		for(int i = 0; i < blocked.size(); i++){
			if(userName.equals(blocked.get(i))){
				blocked.remove(i);
                return true;
			}
		}
        return false;
	}
    
    
}

