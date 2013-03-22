package ss;

public interface Consumer<T> {

	/**
	 * A consumer of items from a ConcurrentBuffer
	 * @param item
	 */
	public void processItem(T item);
	
}
