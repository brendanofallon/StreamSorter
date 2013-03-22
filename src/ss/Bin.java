package ss;

public interface Bin<T> {
	
	public long getStartPosition();
	
	public long getEndPosition();
	
	public void add(T item);
	
	public int size();
	
	public void sortAll();
	
}
