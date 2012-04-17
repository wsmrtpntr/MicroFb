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
	 * onCommand overide, gets commands line by line.
	 */
	@Override
	public void onCommand(String command) {
		if (commands == null) {
			commands = new ArrayList<String>();
			parseAndExecuteCommand(command);
		} else {
			commands.add(command);
		}
	}
	
	/**
	 * Create an user.
	 * @param user
	 */
	public void create(String user) {
		Validate.notNull(user, "Operation aborted. No user provided.");
		client.Create(server, 
					  user, 
					  IoNotify.notify(this, "Create user " + user));
	}

	/**
	 * Login an user.
	 * @param user
	 */
	public void login(String user) {
		Validate.notNull(user, "Operation aborted. No user provided.");
		Validate.isTrue(currentUser == null, "Operation aborted. User " + currentUser + " already logged in.");
		
		tentativeUser = user;
		friends = new ArrayList<String>();
		requests = new ArrayList<String>();
		messages = new ArrayList<String>();
		
		client.Get(server,
     			   user,
     			   IoNotify.notify(this, "Login " + user));
	}

	/**
	 * Logout an user.
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
	 * Request a 'user' to be a friend.
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
	 * Accept 'user' to be a friend/ 'user' must have requested to be a 
	 * friend first.
	 * @param user
	 */
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
	 * 
	 * @param data
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
				friends.add(line.substring(requestPrefix.length()));
			}
		}
	}
	
	public void parseAndExecuteCommand(String command) {
		String[] pieces = command.split(" ");
		
		out.println("********************************************");
		out.println(command);
		out.println("********************************************");
		
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
	
	public void executeNextCommand() {
		if (commands.size() > 0) {
			parseAndExecuteCommand(commands.remove(0));
		}
	}
}
