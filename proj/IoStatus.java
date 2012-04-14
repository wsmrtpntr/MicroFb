
public enum IoStatus {
	FileDoesNotExist(10),
	FileAlreadyExists(11),
	OperationTimeout(20),
	FileTooLarge(30),
	None(1),
	Success(0);
	
	public int code;
	
	IoStatus(int code){
		this.code = code;
	}
	
	public static IoStatus parseInt(int code){
		IoStatus s = IoStatus.None;
		s.code = code;
		return s;
	}

}
