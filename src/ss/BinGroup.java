package ss;

public class BinGroup<T> {

	final long start;
	final long end;
	final BinFactory<T> binFactory;
	
	private Bin<T>[] bins;

	public BinGroup(long startPos, long endPos, int binCount, BinFactory<T> binFactory) {
		this.start = startPos;
		this.end = endPos;
		this.binFactory = binFactory;
		bins = (Bin<T>[])(new Bin[binCount]);
	}
	
	public void addItem(T item, int pos) {
		int bin = getBinIndexForPosition(pos);
		if (bins[bin] == null) {
			bins[bin] = binFactory.createBin(startPosForBin(bin), endPosForBin(bin));
		}
		bins[bin].add(item);
	}
	
	public long startPosForBin(int binIndex) {
		//double binSize = (end-start)/bins.length;
		return (long)(start + (double)binIndex/(double)bins.length*(end-start));
	}
	
	public long endPosForBin(int binIndex) {
		return (long)(start + (double)(binIndex+1)/(double)bins.length*(end-start) - 1);
	}

	public int getBinIndexForPosition(long pos) {
		return (int) ( (double)(pos-start)/(double)(end-start) *bins.length);
	}
	
	
	
}
