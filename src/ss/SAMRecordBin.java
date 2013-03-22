package ss;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.sf.samtools.SAMRecord;

public class SAMRecordBin implements Bin<SAMRecord> {

	final long start;
	final long end;
	
	List<SAMRecord> contents = new ArrayList<SAMRecord>(1024);
	
	public SAMRecordBin(long startPos, long endPos) {
		this.start = startPos;
		this.end = endPos;
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
	public void add(SAMRecord item) {
		contents.add(item);
	}

	@Override
	public int size() {
		return contents.size();
	}

	@Override
	public void sortAll() {
		Collections.sort(contents, new SAMRecordComparator());
	}


	static class SAMRecordComparator implements Comparator<SAMRecord> {

		@Override
		public int compare(SAMRecord arg0, SAMRecord arg1) {
			return arg1.getAlignmentStart() - arg0.getAlignmentStart();
		}
		
	}
}
