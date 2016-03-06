/**
 * ChatServerTester.java
 * Author: Zachary Watanabe-Gastel
 * 
 * Tester class for the server side of the chat system.
 * Class is intended to be run on a network connected server.
 * Pass the server port as the args[0] parameter
 */

import java.io.IOException;

public class ChatServerTester {
	public static void main(String[] args) {
		try {
			ChatServer app = new ChatServer(args[0]);
			app.run();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
}

