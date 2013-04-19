package ss.bin;

import ss.sam.SAMString;

public class SAMStringBinFactory implements BinFactory<SAMString> {

	@Override
	public Bin<SAMString> createBin(long start, long end) {
		return new SAMStringBin(start, end);
	}


}
