package ss;

import java.io.File;
import java.io.IOException;

import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMFileWriter;
import net.sf.samtools.SAMFileWriterFactory;
import net.sf.samtools.SAMRecord;



public class SAMRecordWriter implements Writeable<SAMRecord> {

	SAMFileWriter writer;
	
	//Used to ensure records are sorted properly when writing
	private int prevRefIndex = -1;
	private long prevPos = Integer.MIN_VALUE;

	public SAMRecordWriter(SAMFileWriter writer) {
		this.writer = writer;
	}
	
	public SAMRecordWriter(SAMFileHeader header, File dest) {
		SAMFileWriterFactory factory = new SAMFileWriterFactory();
		writer = factory.makeSAMWriter(header, true, dest);
	}
	
	@Override
	public void write(SAMRecord item) throws IOException {
		
		if (item.getReferenceIndex() < prevRefIndex) {
			throw new IllegalStateException("Records are not being written in reference order");
		}
		if ((prevRefIndex == item.getReferenceIndex()) && (item.getAlignmentStart() < prevPos)) {
			throw new IllegalStateException("Records are not being written in alignment order, ref index: " + item.getReferenceIndex() + " name:" + item.getReferenceName() + " pos: " + item.getAlignmentStart() + " prev record:" + prevPos);
		}
		prevRefIndex = item.getReferenceIndex();
		prevPos = item.getAlignmentStart();
		
		writer.addAlignment(item);
	}

	@Override
	public void close() {
		writer.close();
	}

}
