import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum IoStatus {
	FileDoesNotExist(10),
	FileAlreadyExists(11),
	OperationTimeout(20),
	FileTooLarge(30),
	Success(0);
	
	private static final Map<Integer,IoStatus> lookup 
		= new HashMap<Integer, IoStatus>();

	static {
	    for(IoStatus s : EnumSet.allOf(IoStatus.class))
	         lookup.put(s.getCode(), s);
	}

	private int code;

	private IoStatus(int code) {
	    this.code = code;
	}

	public int getCode() { return code; }

	public static IoStatus parseInt(int code) { 
	    return lookup.get(code); 
	}
}
