import org.apache.commons.lang.Validate;

public class IoNotify implements INotify {
	Facebook fb;
	String action;
	boolean forLogin;
	
	private IoNotify() {
	}
	
	static public INotify notify(
			Facebook fb, 
			String action,
			boolean forLogin) {
		Validate.notNull(fb);
		Validate.notNull(action);
		
		IoNotify notify = new IoNotify();
		notify.fb = fb;
		notify.action = action;
		notify.forLogin = forLogin;
		return notify;		
	}
	
	static public INotify notify(
			Facebook fb, 
			String action) {
		return notify(fb, action, false);
	}


	/**
	 * Called when the command to server has completed.
	 */
	@Override
	public void OnCompleted(IoStatus result, String data) {
		
		if (forLogin && data == null) {
			data = "";
		}
		
		if (data != null) {
			fb.loadUserInfo(data);
		}
		
		if (action != null) {
			fb.printAction(action);
			fb.printResult(result.toString());
		}
	}
}
