package ss.bin;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.sf.samtools.SAMRecord;

import ss.Writeable;


/**
 * A very simple container for SAMRecords that stores everything in memory in an arraylist
 * @author brendanofallon
 *
 */
public class SAMRecordBin extends AbstractSAMRecordBin {

	public SAMRecordBin(long start, long end) {
		super(start, end);
	}


	List<SAMRecord> contents = new ArrayList<SAMRecord>(1024);
	
	

	@Override
	public synchronized void add(SAMRecord item) {
		contents.add(item);
		sorted = false;
	}

	@Override
	public int size() {
		return contents.size();
	}

	@Override
	public synchronized void sortAll() {
		if (! sorted && contents.size()>1) {
			Collections.sort(contents, new SAMRecordComparator());
		}
		sorted = true;
	}	


	@Override
	public void writeItems(Writeable<SAMRecord> output) throws IOException {
		for(SAMRecord rec : contents) {
			output.write(rec);
		}
	}


}
