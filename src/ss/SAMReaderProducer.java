package ss;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import ss.buffer.Producer;

import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMFileReader.ValidationStringency;
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
	private long recordsProduced = 0; 
	
	public SAMReaderProducer(File inputFile) throws FileNotFoundException {
		this(new FileInputStream(inputFile));
	}
	
	public SAMReaderProducer(InputStream stream) {
		SAMFileReader.setDefaultValidationStringency(ValidationStringency.LENIENT);
		reader = new SAMFileReader(stream);
		reader.setValidationStringency(ValidationStringency.LENIENT);
		iterator = reader.iterator();
	}
	
	public SAMFileHeader getHeader() {
		return reader.getFileHeader();
	}
	
	@Override
	public boolean isFinishedProducing() {
		return ! iterator.hasNext();
	}

	@Override
	public SAMRecord nextItem() {
		recordsProduced++;
		return iterator.next();
	}

}
