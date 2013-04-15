package ss;

import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMFileWriterFactory;

public class SAMWriterContext {
	public final SAMFileWriterFactory factory;
	public final SAMFileHeader header;
	
	public SAMWriterContext(SAMFileWriterFactory factory, SAMFileHeader header) {
		this.factory = factory;
		this.header = header;
	}
}
