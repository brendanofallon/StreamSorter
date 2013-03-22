package ss;

public interface BinFactory<T> {

	public Bin<T> createBin(long start, long end);
	
}
