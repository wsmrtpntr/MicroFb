import org.apache.commons.lang.Validate;

public class IoNotify implements INotify {
	Facebook fb;
	String action;
	
	private IoNotify() {
	}
	
	static public INotify notify(
			Facebook fb, 
			String action) {
		Validate.notNull(fb);
		Validate.notNull(action);
		
		IoNotify notify = new IoNotify();
		notify.fb = fb;
		notify.action = action;
		return notify;		
	}

	@Override
	public void OnCompleted(IoStatus result, String data) {
		
		if (data != null) {
			fb.loadUserInfo(data);
		}
		
		if (action != null) {
			fb.printAction(action);
			fb.printResult(result.toString());
		}
		
		fb.executeNextCommand();
	}
}
