package ss.bin;

import java.io.IOException;
import java.util.Comparator;

import ss.Writeable;

import net.sf.samtools.SAMRecord;

public abstract  class AbstractSAMRecordBin implements Bin<SAMRecord> {

	final long start;
	final long end;
	protected boolean sorted = true; 
	
	public AbstractSAMRecordBin(long start, long end) {
		this.start = start;
		this.end = end;
	}
	
	@Override
	public long getStartPosition() {
		return start;
	}

	@Override
	public long getEndPosition() {
		return end;
	}

	@Override
	public abstract void add(SAMRecord item);

	@Override
	public abstract int size();

	@Override
	public abstract void sortAll();

	@Override
	public boolean isSorted() {
		return sorted;
	}

	@Override
	public abstract void writeItems(Writeable<SAMRecord> output) throws IOException;

	
	static class SAMRecordComparator implements Comparator<SAMRecord> {

		@Override
		public int compare(SAMRecord arg0, SAMRecord arg1) {
			return arg0.getAlignmentStart() - arg1.getAlignmentStart();
		}
		
	}
}
