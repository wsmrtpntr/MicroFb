import edu.washington.cs.cse490h.lib.Utility;


public class FileServerTester extends FileServerNode {

	@Override
	public void start() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onCommand(String command) {
		String[] splits = command.split(" ");
		switch(splits[0])
		{
		case "create":
			// TODO what's happening with the ACKs?
			// TODO why only Protocol.RIOTEST_PKT? 
			RIOSend(Integer.parseInt(splits[1]), Protocol.RIOTEST_PKT, Utility.stringToByteArray("create " + splits[2]));
			break;
		default:
			// TODO error for unknown command
			break;
		}
	}

}
