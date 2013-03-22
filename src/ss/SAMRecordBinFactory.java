package ss;

import net.sf.samtools.SAMRecord;

public class SAMRecordBinFactory implements BinFactory<SAMRecord> {

	@Override
	public Bin<SAMRecord> createBin(long start, long end) {
		return new SAMRecordBin(start, end);
	}

}
