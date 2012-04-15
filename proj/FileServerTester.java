import edu.washington.cs.cse490h.lib.Utility;


public class FileServerTester extends FileServerClient {

	@Override
	public void onCommand(String command) {
		String[] splits = command.split(" ");
		switch(splits[0])
		{
		case "create":
			// TODO what's happening with the ACKs?
			// TODO why only Protocol.RIOTEST_PKT? 
			Create(Integer.parseInt(splits[1]), splits[2], new CreateRequest());
			break;
		case "get":
			RIOSend(Integer.parseInt(splits[1]), Protocol.RIOTEST_PKT, Utility.stringToByteArray("get " + splits[2]));
			break;
		default:
			// TODO error for unknown command
			break;
		}
	}
	
	private class CreateRequest implements INotify{
		
		@Override
		public void OnCompleted(IoStatus result, String data) {
			System.out.println("Operation completed with code " + result.toString());
		}
		
	}
	
}
