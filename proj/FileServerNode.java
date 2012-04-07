import edu.washington.cs.cse490h.lib.Utility;


public class FileServerNode extends RIONode {

	public static int FileDoesNotExist = 10 ;
	public static int FileAlreadyExists = 11;
	public static int Timeout = 20;
	public static int FileTooLarge = 130;
	public static int Success = 0;
	
	@Override
	public void onRIOReceive(Integer from, int protocol, byte[] msg) {
		// TODO - define the protocol between the server and the client
		String[] cmds = Utility.byteArrayToString(msg).split(" ");
		switch(cmds[0]){
		case "create":
			int ret = create(cmds[1]);
			// return code to the client
			RIOSend(from, Protocol.DATA, Utility.stringToByteArray(Integer.toString(ret)));
			break;
		default:
			logSynopticEvent("FileServer: Unknown command:" + cmds[0]);
			// TODO log commands we did not understand
			break;
		}
	}

	@Override
	public void start() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onCommand(String command) {
		// TODO Auto-generated method stub
	}

	/*
	 * 	This command creates an empty file called filename. If the file already exists, 
	 * this should fail in the manner described	below.
	 */
	private int create(String filename)
	{
		// create a new file. Need to use PersistentStorageWriter
		return Success;
	}
}
