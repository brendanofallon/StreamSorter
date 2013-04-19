package ss.bin;

import net.sf.samtools.SAMRecord;
import ss.SAMWriterContext;

public class SAMRecordBinFactory implements BinFactory<SAMRecord> {

	final SAMWriterContext context;
	
	public SAMRecordBinFactory(SAMWriterContext ctxt) {
		this.context = ctxt;
	}
	
	@Override
	public Bin<SAMRecord> createBin(long start, long end) {
		return new KryoBin(start, end, context);
		//return new HybridBin(start, end, context);
	}
	




}
