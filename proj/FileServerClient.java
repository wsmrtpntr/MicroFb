import java.lang.reflect.Method;
import java.util.HashMap;

import edu.washington.cs.cse490h.lib.Callback;
import edu.washington.cs.cse490h.lib.Utility;


public class FileServerClient extends FileServerNode {

	// id of the next request
	private int nextRequest = 0;
	private int createResult = -1;
	private HashMap<Integer, INotify> pendingRequests = new HashMap<Integer, INotify>();
	
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
				Integer requestId = Integer.parseInt(pieces[2]);
				IoStatus status = IoStatus.parseInt(Integer.parseInt(pieces[3]));
				NotifyAndRemove(requestId, status);
				break;
			default: break;
		}
	}
	
	
	public int Create(int destAddr, String filename, INotify onCompleted)
	{
		int requestId = GetNextId();
		RIOSend(destAddr, Protocol.RIOTEST_PKT, Utility.stringToByteArray("create " + String.valueOf(requestId) + " " + filename));
		pendingRequests.put(requestId, onCompleted);
		
		// register the timeout
		Method onTimeoutMethod = null;
		try {
			onTimeoutMethod = Callback.getMethod("onTimeout", this, new String[]{ "java.lang.Integer"});
		} catch (ClassNotFoundException | NoSuchMethodException
				| SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		addTimeout(new Callback(onTimeoutMethod, this, new Object[]{ requestId }), ReliableInOrderMsgLayer.TIMEOUT);
		
		// send heart beats to move the clock
		RIOSend(destAddr, Protocol.RIOTEST_PKT, Utility.stringToByteArray("heartbeat"));
		RIOSend(destAddr, Protocol.RIOTEST_PKT, Utility.stringToByteArray("heartbeat"));
		RIOSend(destAddr, Protocol.RIOTEST_PKT, Utility.stringToByteArray("heartbeat"));

		return createResult;
	}
	
	public void onTimeout(Integer requestId)
	{
		NotifyAndRemove(requestId, IoStatus.OperationTimeout);
	}
	
	private synchronized int GetNextId(){
		return nextRequest++;
	}
	
	private synchronized void NotifyAndRemove(Integer requestId, IoStatus status){
		if(pendingRequests.containsKey(requestId)){
			pendingRequests.get(requestId).OnCompleted(status);
			pendingRequests.remove(requestId);
		}
	}
}
