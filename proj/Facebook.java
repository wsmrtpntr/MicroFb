import java.io.PrintStream;
import java.util.ArrayList;
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
    Facebook client;
    PrintStream out;
    List<String> commands;
    
    //
    // User data.
    //

    String tentativeUser;
	String currentUser;
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
	
	public Facebook() {
		this.client = this;
		this.server = 0;
		out = System.out;
		currentUser = null;
		
	}

	/**
	 * onCommand override, gets commands line by line. Dispatch the facebook 
	 * commands.
	 */
	@Override
	public void onCommand(String command) {
		String[] pieces = command.split(" ");
		
		out.println("********************************************");
		out.println("Initiate " + command);
		out.println("********************************************");
		
		if(pieces[0].equals("create")) {
			this.create(pieces[1]);
		} else if(pieces[0].equals("login")) {
			this.login(pieces[1]);
		} else if(pieces[0].equals("logout")) {
			this.logout();
		} else if(pieces[0].equals("request")) {
			this.requestFriend(pieces[1]);
		} else if(pieces[0].equals("accept")) {
			this.acceptFriend(pieces[1]);
		} else if(pieces[0].equals("post")) {
			this.postToAll(pieces[1]);
		} else if(pieces[0].equals("read")) {
			this.readAll();
		}
	}
	
	/**
	 * Create an user. Create the <user> file.
	 * @param user
	 */
	public void create(String user) {
		Validate.notNull(user, "Operation aborted. No user provided.");
		client.Create(server, 
					  user, 
					  IoNotify.notify(this, "Create user " + user));
	}

	/**
	 * Login an user. Read the user file and call loadUserInfo to initialize the 
	 * friends, requests and messages lists. Store the current user.
	 * @param user
	 */
	public void login(String user) {
		Validate.notNull(user, "Operation aborted. No user provided.");
		Validate.isTrue(currentUser == null, "Operation aborted. User " + currentUser + " already logged in.");
		
		tentativeUser = user;
		
		client.Get(server,
     			   user,
     			   IoNotify.notify(this, "Login " + user, true));
	}

	/**
	 * Logout an user. Forget the curent user.
	 */
	public void logout() {
		Validate.notNull(tentativeUser, "Operation aborted. No user logged in.");
		
		tentativeUser = null;
		currentUser = null;
		friends = null;
		requests = null;
		messages = null;

		printAction("Logout " + currentUser);
		printResult("Success");
	}

	/**
	 * Request a 'user' to be a friend. Write in his <user> file the words:
	 * 'Request <currentUser>' 
	 * @param user
	 */
	public void requestFriend(String user) {
		Validate.notNull(user, "Operation aborted. No user provided.");
		Validate.notNull(currentUser, "Operation aborted. No user logged in.");
		client.Append(server,
  			          user,
  			          requestPrefix + currentUser + delimiter,
  			          IoNotify.notify(this, "Request friend " + user));
	}

	/**
	 * Accept 'user' to be a friend. 'user' must have requested to be a 
	 * friend first. Write in the <currentUser> file the words: Friend:<user>.
	 * @param user
	 */
	public void acceptFriend(String user) {
		Validate.notNull(user, "Operation aborted. No user provided.");
		Validate.notNull (currentUser, "Operation aborted. No user logged in.");
		Validate.isTrue (requests.contains(user), "No request found from " + user + ".");
		Validate.isTrue (!friends.contains(user), user + " is already a frind.");
		requests.remove(user);
		friends.add(user);
		client.Append (server,
		               currentUser,
		               friendPrefix + user + delimiter,
		               IoNotify.notify(this, "Accept friend " + user));
	}

	/**
	 * Post a message to all friends. For every friend write in his file the words
	 * Message:<message>. 
	 * @param message
	 */
	public void postToAll(String message) {
		Validate.notNull(message, "Operation aborted. No message provided.");
		Validate.notNull(currentUser, "Operation aborted. No user logged in.");
		for(String user : friends) {
			client.Append (server,
		                   user,
		                   messagePrefix + message + delimiter,
		                   IoNotify.notify(this, "Send message to " + user));
		}
	}

	/**
	 * Print all messages. The message list was already loaded at login.
	 * @return
	 */
	public String[] readAll() {
		Validate.notNull(currentUser, "Operation aborted. No user logged in.");
		for(String message : messages) {
			out.println(message);
		}
		return null;
	}

	public void setUser(String user) {
		this.currentUser = user;
	}
	
	/**
	 * 
	 * @param text
	 */
	public void printAction(String text) {
		out.print("*** " + text + ". ");
	}

	/**
	 * 
	 * @param text
	 */
	public void printResult(String text) {
		out.println(text + ".");
	}

	/**
	 * Parse the data froom the <user> file to construct the friends, requests 
	 * and messages lists.
	 * @param data - contents of the <user> file.
	 */
	public void loadUserInfo(String data) {

		if (tentativeUser == null) {
			
			//
			// No user.
			//
			
			return;
		}
		currentUser = tentativeUser;
		friends = new ArrayList<String>();
		requests = new ArrayList<String>();
		messages = new ArrayList<String>();
	
		String[] lines = data.split("\n");
		for(String line : lines) {
			if(line.startsWith(messagePrefix)) {
				messages.add(line.substring(messagePrefix.length()));
			} else if(line.startsWith(friendPrefix)) {
				friends.add(line.substring(friendPrefix.length()));
			} else if(line.startsWith(requestPrefix)) {
				requests.add(line.substring(requestPrefix.length()));
			}
		}
	}
}
