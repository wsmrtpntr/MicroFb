import java.lang.reflect.Method;
import java.util.HashMap;

import edu.washington.cs.cse490h.lib.Callback;
import edu.washington.cs.cse490h.lib.Utility;


public class FileServerClient extends FileServerNode {

	// id of the next request
	private int nextRequest = 0;
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
			case "append":
			case "put":
			case "delete": {
				Integer requestId = Integer.parseInt(pieces[2]);
				IoStatus status = IoStatus.parseInt(Integer.parseInt(pieces[3]));
				NotifyAndRemove(requestId, status);
				}
				break;
				
			case "get" : {
				Integer requestId = Integer.parseInt(pieces[2]);
				IoStatus status = IoStatus.parseInt(Integer.parseInt(pieces[3]));
				CompleteRequest(requestId, status, pieces.length >= 5 ? pieces[4] : null);				
				}
			break;
			
			default: break;
		}
	}
	
	
	public void Create(int destAddr, String filename, INotify onCompleted)
	{
		int requestId = GetNextId();
		RIOSend(destAddr, Protocol.RIOTEST_PKT, Utility.stringToByteArray("create " + String.valueOf(requestId) + " " + filename));
		RegisterPendingRequest(requestId, destAddr, onCompleted);
	}
	
	public void Get(int destAddr, String filename, INotify onCompleted)
	{
		int requestId = GetNextId();
		RIOSend(destAddr, Protocol.RIOTEST_PKT, Utility.stringToByteArray("get " + String.valueOf(requestId) + " " + filename));
		RegisterPendingRequest(requestId, destAddr, onCompleted);
	}
	
	public void Delete(int destAddr, String filename, INotify onCompleted)
	{
		int requestId = GetNextId();
		RIOSend(destAddr, Protocol.RIOTEST_PKT, Utility.stringToByteArray("delete " + String.valueOf(requestId) + " " + filename));
		RegisterPendingRequest(requestId, destAddr, onCompleted);
	}
	
	public void Append(int destAddr, String filename, String data, INotify onCompleted)
	{
		int requestId = GetNextId();
		RIOSend(
				destAddr, 
				Protocol.RIOTEST_PKT, 
				Utility.stringToByteArray("append " + String.valueOf(requestId) + " " + filename + " " + data));
		RegisterPendingRequest(requestId, destAddr, onCompleted);
	}
	
	public void Put(int destAddr, String filename, String data, INotify onCompleted)
	{
		int requestId = GetNextId();
		RIOSend(
				destAddr, 
				Protocol.RIOTEST_PKT, 
				Utility.stringToByteArray("put " + String.valueOf(requestId) + " " + filename + " " + data));
		RegisterPendingRequest(requestId, destAddr, onCompleted);
	}
	
	private void RegisterPendingRequest(int requestId, int destAddr, INotify onCompleted) {
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
	}
	
	public void onTimeout(Integer requestId)
	{
		NotifyAndRemove(requestId, IoStatus.OperationTimeout);
	}
	
	private synchronized int GetNextId(){
		return nextRequest++;
	}
	
	private synchronized void NotifyAndRemove(Integer requestId, IoStatus status){
		CompleteRequest(requestId, status, null);
	}
	
	private synchronized void CompleteRequest(Integer requestId, IoStatus status, String result){
		if(pendingRequests.containsKey(requestId)){
			pendingRequests.get(requestId).OnCompleted(status, result);
			pendingRequests.remove(requestId);
		}
	}
}
