/**
 * ChatClientTester.java
 * Author: Zachary Watanabe-Gastel
 * 
 * This class initiates the chat client. It is also class where tests will be written.
 */
 
import java.io.IOException;

//Tester class for Chat Client
public class ChatClientTester {
	public static void main(String[] args) {
       		if(args.length < 2){
            		System.out.println("Usage is: java ChatClientTester <server ip> <server port>");
        	}
		try {
			ChatClient client = new ChatClient(args[0], args[1]);
			client.run();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
