package ss;

import java.io.InputStream;

import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMRecordIterator;

/**
 * A Producer that reads sam records from an input stream. 
 * @author brendan
 *
 */
public class SAMReaderProducer implements Producer<SAMRecord> {

	final SAMFileReader reader;
	private SAMRecordIterator iterator;
	
	public SAMReaderProducer(InputStream stream) {
		reader = new SAMFileReader(stream);
		iterator = reader.iterator();
	}
	
	
	@Override
	public boolean isFinishedProducing() {
		return ! iterator.hasNext();
	}

	@Override
	public SAMRecord nextItem() {
		return iterator.next();
	}

}
