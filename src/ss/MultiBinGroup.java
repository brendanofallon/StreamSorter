package ss;

import java.io.IOException;

import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMSequenceRecord;
import ss.bin.BinFactory;
import ss.bin.BinGroup;

public class MultiBinGroup {
	
	private BinGroup<SAMRecord>[] binGroups;
	
	public MultiBinGroup(SAMFileHeader header, BinFactory<SAMRecord> binFactory) {
		binGroups = (BinGroup<SAMRecord>[]) new BinGroup[ header.getSequenceDictionary().getSequences().size() ];
		for(SAMSequenceRecord seq : header.getSequenceDictionary().getSequences()) {
			int binCount = computeBinNumber(seq.getSequenceLength());
			//System.out.println("Contig: " + seq.getSequenceName() + " length: " + seq.getSequenceLength() + " bins:" + binCount);
			BinGroup<SAMRecord> bg = new BinGroup<SAMRecord>(0L, (long)seq.getSequenceLength(), binCount, binFactory);
			binGroups[seq.getSequenceIndex()] = bg;
		}
	}
	
	private static int computeBinNumber(long length) {
		int bins=  (int) (length/5e6);
		return Math.max(bins, 1);
	}
	
	public void addRecord(SAMRecord rec) {
		if (rec.getReferenceIndex()>-1) { 
			binGroups[rec.getReferenceIndex()].addItem(rec, rec.getAlignmentStart());
		}
	}
	
	public void emitAll() {
		System.out.println("MultiBinGroup : " + binGroups.length + " total groups");
		for(int i=0; i<binGroups.length; i++) {
			binGroups[i].emitState();
		}
	}
	
	public int getBinGroupCount() {
		return binGroups.length;
	}
	
	public BinGroup<SAMRecord> getBinGroup(int which) {
		return binGroups[which];
	}
	
	public void writeAll(Writeable<SAMRecord> writer) throws IOException {
		for(int i=0; i<binGroups.length; i++) {
			binGroups[i].writeAll(writer);
		}
	}
}
