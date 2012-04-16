



import java.io.PrintStream;
import java.util.List;

import org.apache.commons.lang.Validate;




/**
 * @author web
 *
 */
public class Facebook extends FileServerClient {
	
	//
	// Communication data.
	//

	int server;
    FileServerClient client;
    PrintStream out;
    
    //
    // User data.
    //
    
	String currentUser = null;
	List<String> friends;
	List<String> requests;
	List<String> messages;
	
	//
	// Static data.
	//

	static final private String friendPrefix = "Friend:";
	static final private String requestPrefix = "Request:";
	static final private String messagePrefix = "Message:";
	static final private char delimiter = '\n';
	
	//
	// Implementation.
	//
	
	public Facebook(int server, FileServerClient client) {
		this.client = client;
		this.server = server;
		out = System.out;
	}
	
	@Override
	public void onCommand(String command) {
		String[] pieces = command.split(" ");
		
		if(pieces[0].equals("create")) {
			this.create(pieces[1]);
		} else if(pieces[0].equals("login")) {
			this.login(pieces[1]);
		} else if(pieces[0].equals("logout")) {
			this.logout();
		} else if(pieces[0].equals("request-friend")) {
			this.requestFriend(pieces[1]);
		} else if(pieces[0].equals("accept-friend")) {
			this.acceptFriend(pieces[1]);
		} else if(pieces[0].equals("post-to-all")) {
			this.postToAll(pieces[1]);
		} else if(pieces[0].equals("read-all")) {
			this.readAll();
		}
	}
	
	public void create(String user) {
		Validate.notNull(user, "Operation aborted. No user provided.");
		client.Create(server, 
					  user, 
					  IoNotify.notify(this, "Create user " + user));
	}

	public void login(String user) {
		Validate.notNull(user, "Operation aborted. No user provided.");
		Validate.isTrue(currentUser == null, "Operation aborted. User " + currentUser + " already logged in.");
		client.Get(server,
     			   user,
     			   IoNotify.notify(this, "Login " + user));
	}

	public void logout() {
		Validate.notNull(currentUser, "Operation aborted. No user logged in.");
		currentUser = null;
		printAction("Logout " + currentUser);
		printResult("Success");
	}

	public void requestFriend(String user) {
		Validate.notNull(user, "Operation aborted. No user provided.");
		Validate.notNull(currentUser, "Operation aborted. No user logged in.");
		client.Append(server,
  			          user,
  			          requestPrefix + currentUser + delimiter,
  			          IoNotify.notify(this, "Request friend " + user));
	}

	public void acceptFriend(String user) {
		Validate.notNull(user, "Operation aborted. No user provided.");
		Validate.notNull (currentUser, "Operation aborted. No user logged in.");
		Validate.isTrue (requests.contains(user), "No request found from" + user + ".");
		Validate.isTrue (!friends.contains(user), user + "is already a frind.");
		client.Append (server,
		               currentUser,
		               friendPrefix + currentUser + delimiter,
		               IoNotify.notify(this, "Accept friend " + user));
	}

	public void postToAll(String message) {
		Validate.notNull(message, "Operation aborted. No message provided.");
		Validate.notNull(currentUser, "Operation aborted. No user logged in.");
		for(String user : friends) {
			client.Append (server,
		                   user,
		                   messagePrefix + currentUser + delimiter,
		                   IoNotify.notify(this, "Send message to " + user));
		}
	}

	public String[] readAll() {
		Validate.notNull(currentUser, "Operation aborted. No user logged in.");
		return messages.toArray(null);
	}

	public void setUser(String user) {
		this.currentUser = user;
	}
	
	/**
	 * 
	 * @param text
	 */
	public void printAction(String text) {
		out.print(text + ". ");
	}

	/**
	 * 
	 * @param text
	 */
	public void printResult(String text) {
		out.println(text + ".");
	}

	/**
	 * 
	 * @param data
	 */
	public void loadUserInfo(String data) {
		String[] lines = data.split("\n");
		for(String line : lines) {
			if(line.startsWith(messagePrefix)) {
				messages.add(line.substring(messagePrefix.length()));
			} else if(line.startsWith(friendPrefix)) {
				friends.add(line.substring(friendPrefix.length()));
			} else if(line.startsWith(requestPrefix)) {
				friends.add(line.substring(requestPrefix.length()));
			}
		}
	}
}
