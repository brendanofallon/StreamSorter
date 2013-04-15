package ss.bin;

import java.io.IOException;

import ss.Writeable;

public interface Bin<T> {
	
	public long getStartPosition();
	
	public long getEndPosition();
	
	public void add(T item);
	
	public int size();
	
	public void sortAll();
	
	public boolean isSorted();
	
	public void writeItems(Writeable<T> output) throws IOException;
	
}
