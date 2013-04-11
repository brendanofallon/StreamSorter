package ss;

import java.io.File;
import java.io.IOException;

import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMFileWriter;
import net.sf.samtools.SAMFileWriterFactory;
import net.sf.samtools.SAMRecord;



public class SAMRecordWriter implements Writeable<SAMRecord> {

	SAMFileWriter writer;

	public SAMRecordWriter(SAMFileWriter writer) {
		this.writer = writer;
	}
	
	public SAMRecordWriter(SAMFileHeader header, File dest) {
		SAMFileWriterFactory factory = new SAMFileWriterFactory();
		writer = factory.makeSAMWriter(header, true, dest);
	}
	
	@Override
	public void write(SAMRecord item) throws IOException {
		writer.addAlignment(item);
	}

	@Override
	public void close() {
		writer.close();
	}

}
