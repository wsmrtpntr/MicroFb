
public interface INotify {
	public void OnCompleted(IoStatus result);
	public void OnCompleted(IoStatus result, byte[] data);
}
