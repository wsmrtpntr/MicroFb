import java.io.IOException;
import edu.washington.cs.cse490h.lib.Utility;


public class FileServerNode extends RIONode {

	public static int FileDoesNotExist = 10 ;
	public static int FileAlreadyExists = 11;
	public static int Timeout = 20;
	public static int FileTooLarge = 130;
	public static int Success = 0;
	
	@Override
	public void onRIOReceive(Integer from, int protocol, byte[] msg) {
		processMessage(from, protocol, msg);
	}

	@Override
	public void start() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onCommand(String command) {
		// TODO Auto-generated method stub
	}
	
	protected void processMessage(Integer from, int protocol, byte[] msg) {
		String[] cmds = Utility.byteArrayToString(msg).split(" ");
		switch(cmds[0]){
		case "create":
			IoStatus ret = create(cmds[2]);
			RIOSend(from, 
					Protocol.RIOTEST_PKT, 
					Utility.stringToByteArray("acknowledge create " + cmds[1] + " " + Integer.toString(ret.code)));
			break;
		case "heartbeat":
			// ignore the heart beats
			break;
		default:
			break;
		}
	}

	/*
	 * 	This command creates an empty file called filename. If the file already exists, 
	 * this should fail in the manner described	below.
	 */
	private IoStatus create(String filename)
	{
		if(Utility.fileExists(this, filename)){
			return IoStatus.FileAlreadyExists;
		}
		
		try {
			getWriter(filename, false);
		} catch (IOException e) {
			return IoStatus.FileAlreadyExists;
		}
		
		return IoStatus.Success;
	}
}
