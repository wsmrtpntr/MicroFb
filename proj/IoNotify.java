




import org.apache.commons.lang.Validate;




public class IoNotify implements INotify {
	Facebook commandsImpl;
	String action;
	
	private IoNotify() {
	}
	
	static public INotify notify(
			Facebook commandsImpl, 
			String action) {
		Validate.notNull(commandsImpl);
		Validate.notNull(action);
		
		IoNotify notify = new IoNotify();
		notify.commandsImpl = commandsImpl;
		notify.action = action;
		return notify;		
	}

	@Override
	public void OnCompleted(IoStatus result, String data) {
		
		if (data != null) {
			commandsImpl.loadUserInfo(data);
		}
		
		if (action != null) {
			commandsImpl.printAction(action);
			commandsImpl.printResult(result.toString());
		}
	}
}
