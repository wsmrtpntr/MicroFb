import java.io.FileNotFoundException;
import java.io.IOException;

import edu.washington.cs.cse490h.lib.PersistentStorageReader;
import edu.washington.cs.cse490h.lib.PersistentStorageWriter;
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
		//super.start();
		
		// TODO Auto-generated method stub
		/*if .temp exists
		 PSReader temp = getReader(.temp)
		 if (!temp.ready())
		   delete temp
		 else
		   filename = temp.readLine()
		   oldContents = read rest of temp
		   PSWriter revertFile = getWriter(filename, false)
		   revertFile.write(oldContents)
		   delete temp
		   */
		
		// see if we need to recover any operation if node failed
		PersistentStorageReader tempReader;
		try {
			tempReader = getReader(".temp");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			return;
		}
		
		try {
			if(!tempReader.ready()) {
				// delete temporary file
				tempReader.close();
				PersistentStorageWriter writer = getWriter(".temp", true);
				writer.delete();
			} else {
				String filename = tempReader.readLine();
				PersistentStorageWriter writer = getWriter(filename,  false);
				StringBuilder rest = new StringBuilder();
				ReadAsString(rest, tempReader);
				writer.write(rest.toString());
				writer.close();
				
				// delete temporary file
				tempReader.close();
				PersistentStorageWriter tempwriter = getWriter(".temp", true);
				tempwriter.delete();
				tempwriter.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onCommand(String command) {
		// TODO Auto-generated method stub
	}
	
	protected void processMessage(Integer from, int protocol, byte[] msg) {
		String[] cmds = Utility.byteArrayToString(msg).split(" ");
		if (cmds[0].equals("create")) {
			IoStatus ret = create(cmds[2]);
			RIOSend(from, 
					Protocol.RIOTEST_PKT, 
					Utility.stringToByteArray("acknowledge create " + cmds[1] + " " + Integer.toString(ret.getCode())));
		} else if (cmds[0].equals("get")) {
			StringBuilder results = new StringBuilder();
			IoStatus ret = get(cmds[2], results);
			RIOSend(from, 
					Protocol.RIOTEST_PKT, 
					Utility.stringToByteArray("acknowledge get " + cmds[1] + " " + Integer.toString(ret.getCode()) + " " + results.toString()));
		} else if (cmds[0].equals("append")) {
			IoStatus ret = append(cmds[2], cmds[3]);
			RIOSend(from, 
					Protocol.RIOTEST_PKT, 
					Utility.stringToByteArray("acknowledge append " + cmds[1] + " " + Integer.toString(ret.getCode())));
		} else if (cmds[0].equals("delete")) {
			IoStatus ret = delete(cmds[2]);
			RIOSend(from, 
					Protocol.RIOTEST_PKT, 
					Utility.stringToByteArray("acknowledge delete " + cmds[1] + " " + Integer.toString(ret.getCode())));
		} else if (cmds[0].equals("put")) {
			IoStatus ret = put(cmds[2], cmds[3]);
			RIOSend(from, 
					Protocol.RIOTEST_PKT, 
					Utility.stringToByteArray("acknowledge put " + cmds[1] + " " + Integer.toString(ret.getCode())));
		} else if (cmds[0].equals("heartbeat")) {
			// ignore the heart beats
			;
		} else {
			;
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
	
	private IoStatus get(String filename, StringBuilder results){

		PersistentStorageReader reader;
		try {
			reader = getReader(filename);
		} catch (FileNotFoundException e1) {
			return IoStatus.FileDoesNotExist;
		}
		
		ReadAsString(results, reader);
		
		return IoStatus.Success;
	}

	private void ReadAsString(StringBuilder results,
			PersistentStorageReader reader) {
		char[] charbuf = new char[4096];
		try {
			int cbRead = 0;
			while( (cbRead = reader.read(charbuf, 0, 4096))  != -1){
				results.append(charbuf, 0, cbRead);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private IoStatus append(String filename, String data)
	{
		if(!Utility.fileExists(this, filename)){
			return IoStatus.FileDoesNotExist;
		}
		
		try {
			PersistentStorageWriter writer = getWriter(filename, true);
			writer.append(data);
		} catch (IOException e) {
			return IoStatus.FileDoesNotExist;
		}
		
		return IoStatus.Success;
	}
	
	private IoStatus put(String filename, String data)
	{
		StringBuilder fileContents = new StringBuilder();
		IoStatus getResult = get(filename, fileContents);
		if(getResult != IoStatus.Success) {
			return getResult;
		}

		PersistentStorageWriter tempWriter = null;
		try {
			tempWriter = getWriter(".temp", false);
			tempWriter.write(filename);
			tempWriter.write("\n");
			tempWriter.write(fileContents.toString());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return IoStatus.FileAlreadyExists;
		}
		
		try {
			// TODO implement put so it's resilient to node failures
			PersistentStorageWriter writer = getWriter(filename, false);
			writer.write(data);
			tempWriter.delete();
		} catch (IOException e) {
			e.printStackTrace();
			return IoStatus.FileDoesNotExist;
		}
		
		return IoStatus.Success;
	}
	
	private IoStatus delete(String filename)
	{
		if(!Utility.fileExists(this, filename)){
			return IoStatus.FileDoesNotExist;
		}
			
		try {
			// TODO implement put so it's resilient to node failures
			PersistentStorageWriter writer = getWriter(filename, true);
			writer.delete();
		} catch (IOException e) {
			return IoStatus.FileDoesNotExist;
		}
		
		return IoStatus.Success;
	}
	
	
}
