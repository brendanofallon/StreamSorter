package ss.bin;

import java.io.IOException;

import ss.Writeable;

/**
 * An array of adjacent bins that spans a region from startPos to endPos
 * @author brendanofallon
 *
 * @param <T>
 */
public class BinGroup<T> {

	final long start;
	final long end;
	final BinFactory<T> binFactory;
	private int recordsAdded = 0;
	private Bin<T>[] bins;

	public BinGroup(long startPos, long endPos, int binCount, BinFactory<T> binFactory) {
		this.start = startPos;
		this.end = endPos;
		this.binFactory = binFactory;
		bins = (Bin<T>[])(new Bin[binCount]);
	}
	
	public void addItem(T item, int pos) {
		int bin = getBinIndexForPosition(pos);
		if (bin >= bins.length) {
			System.err.println("Arrgh, bin # is too big for this group, bin# " + bin + " pos: " + pos);
		}
		else {
			if (bins[bin] == null) {
				bins[bin] = binFactory.createBin(startPosForBin(bin), endPosForBin(bin));
			}

			bins[bin].add(item);
			recordsAdded++;
		}
	}
	
	/**
	 * The number of items so far added to this group
	 * @return
	 */
	public int size() {
		return recordsAdded;
	}
	/**
	 * Write all bins in ascending order to the given writer
	 * @param writer
	 * @throws IOException
	 */
	public void writeAll(Writeable<T> writer) throws IOException {
		for(int i=0; i<bins.length; i++) {
			Bin<T> bin = bins[i];
			if (bin != null) {
				bin.writeItems(writer);
			}
		}
	}
	
	
	public int getBinCount() {
		return bins.length;
	}
	
	public Bin<T> getBin(int which) {
		return bins[which];
	}
	
	public void emitState() {
		System.out.println("BinGroup " + start + "-" + end + " bin count: " + bins.length + " size: " + size());
		for(int i=0; i<bins.length; i++) {
			Bin bin = bins[i];
			if (bin != null) {
				System.out.println(startPosForBin(i) + " : used=true, sorted=" + bin.isSorted() + " items:" + bin.size());
			}
			else {
				System.out.println(startPosForBin(i) + " : used=false");
			}
		}
	}
	
	public long startPosForBin(int binIndex) {
		return (long)(start + (double)binIndex/(double)bins.length*(end-start));
	}
	
	public long endPosForBin(int binIndex) {
		return (long)(start + (double)(binIndex+1)/(double)bins.length*(end-start) - 1);
	}

	public int getBinIndexForPosition(long pos) {
		return (int) ( (double)(pos-start)/(double)(end-start) *bins.length);
	}
	
	
	
}
