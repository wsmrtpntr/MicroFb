
public class FileServerTester extends FileServerClient {

	@Override
	public void onCommand(String commandLine) {
		String[] splits = commandLine.split(" ");
		String command = splits[0];
		int server = Integer.parseInt(splits[1]);
		String filename = splits[2];
		String contents = null;
		if (splits.length == 4) {
			contents = splits[3];
		}
		Request request = new Request(
				this.addr, 
				command, 
				server, 
				filename, 
				contents); 
		
		if (command.equals("create")) {
			Create(server, filename, request);
		} else if (command.equals("get")) {
			Get(server, filename, request);
		} else if (command.equals("put")) {
			Put(server, filename, contents, request);
		} else if (command.equals("append")) {
			Append(server, filename, contents, request);
		} else if (command.equals("delete")) {
			Delete(server, filename, request);
		}
	}
	
	private class Request implements INotify{
		int node;
		String command;
		int server;
		String filename;
		String contents;
		
		public Request(
				int node, 
				String command, 
				int server, 
				String filename,
				String contents) {
			this.node = node;
			this.command = command;
			this.server = server;
			this.filename = filename;
			this.contents = contents;
		}
		
		@Override
		public void OnCompleted(IoStatus result, String data) {
			System.out.println("************************************************");
			System.out.println(
					String.format("Node %d: Error: %s on server %d and file %s returned error code %d.",
							node,
							command,
							server,
							filename,
							result.getCode()));
			if (contents != null) {
				System.out.println(
						String.format("Contents: %s", contents));						
			}
		}
		
	}
	
}
