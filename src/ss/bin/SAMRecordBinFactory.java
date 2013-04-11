package ss.bin;

import ss.SAMWriterContext;
import net.sf.samtools.SAMRecord;

public class SAMRecordBinFactory implements BinFactory<SAMRecord> {

	final SAMWriterContext context;
	
	public SAMRecordBinFactory(SAMWriterContext ctxt) {
		this.context = ctxt;
	}
	
	@Override
	public Bin<SAMRecord> createBin(long start, long end) {
		return new HybridBin(start, end, context);
	}
	




}
