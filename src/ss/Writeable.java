package ss;

import java.io.IOException;

/**
 * Label for things that can write a Bin to some sort of output.  
 * @author brendanofallon
 *
 */
public interface Writeable<T> {

	/**
	 * Write the item to some output destination
	 * @param item
	 * @throws IOException
	 */
	public void write(T item) throws IOException;
	
	/**
	 * Close any resources
	 */
	public void close();
	
}
