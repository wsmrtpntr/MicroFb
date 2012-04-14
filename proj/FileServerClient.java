import java.lang.reflect.Method;

import edu.washington.cs.cse490h.lib.Callback;
import edu.washington.cs.cse490h.lib.Utility;


public class FileServerClient extends FileServerNode {

	private boolean createAcked = false;
	private int createResult = -1;
	
	@Override
	public void start() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onRIOReceive(Integer from, int protocol, byte[] msg) {
		
		String[] pieces = Utility.byteArrayToString(msg).split(" ");
		boolean res = pieces[0].equalsIgnoreCase("acknowledge");
		
		if(!res){
			// this message is for the server
			super.processMessage(from, protocol, msg);
			return;
		}
		
		switch(pieces[1]){
			case "create":
				createResult = Integer.parseInt(pieces[2]);
				SetCreateAck();
				break;
			default: break;
		}
	}
	
	@Override
	public void onCommand(String command) {
		String[] splits = command.split(" ");
		switch(splits[0])
		{
		case "create":
			// TODO what's happening with the ACKs?
			// TODO why only Protocol.RIOTEST_PKT? 
			Create(Integer.parseInt(splits[1]), splits[2]);
			break;
		case "get":
			RIOSend(Integer.parseInt(splits[1]), Protocol.RIOTEST_PKT, Utility.stringToByteArray("get " + splits[2]));
			break;
		default:
			// TODO error for unknown command
			break;
		}
	}
	
	
	public int Create(int destAddr, String filename)
	{
		ResetCreateAck();
		RIOSend(destAddr, Protocol.RIOTEST_PKT, Utility.stringToByteArray("create " + filename));
		
		// register the timeout
		Method onTimeoutMethod = null;
		try {
			onTimeoutMethod = Callback.getMethod("onCreateTimeout", this, new String[]{ "java.lang.Integer", "java.lang.String" });
		} catch (ClassNotFoundException | NoSuchMethodException
				| SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		addTimeout(new Callback(onTimeoutMethod, this, new Object[]{ destAddr, filename }), ReliableInOrderMsgLayer.TIMEOUT);
		
		// send heart beats to move the clock
		RIOSend(destAddr, Protocol.RIOTEST_PKT, Utility.stringToByteArray("heartbeat"));
		RIOSend(destAddr, Protocol.RIOTEST_PKT, Utility.stringToByteArray("heartbeat"));
		RIOSend(destAddr, Protocol.RIOTEST_PKT, Utility.stringToByteArray("heartbeat"));

		return createResult;
	}
	
	public void onCreateTimeout(Integer destAddr, String filename)
	{
		if( hasCreateCompleted()){
			// write success to the console
			System.out.println("create completed");
		}
		else{
			System.out.println("create timed out");
		}
	}
	
	public void OnCreateCompleted(int result){
		
	}

	private synchronized Boolean hasCreateCompleted(){
		return createAcked;
	}
	
	private synchronized void SetCreateAck(){
		createAcked = true;
		notifyAll();
	}
	
	private synchronized void ResetCreateAck(){
		createAcked = false;
	}
}
