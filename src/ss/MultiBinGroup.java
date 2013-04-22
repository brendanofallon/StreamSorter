package ss;

import java.io.IOException;

import ss.bin.BinFactory;
import ss.bin.BinGroup;
import ss.sam.SAMString;

public class MultiBinGroup {
	
	private BinGroup<SAMString>[] binGroups;
	
	public MultiBinGroup(SAMReferenceDictionary dict, BinFactory<SAMString> binFactory) {
		binGroups = (BinGroup<SAMString>[]) new BinGroup[ dict.getSequenceCount() ];
		for(String seqName : dict.getSequenceNames()) {
			int binCount = computeBinNumber(dict.getSequenceLength(seqName));
			//System.out.println("Contig: " + seq.getSequenceName() + " length: " + seq.getSequenceLength() + " bins:" + binCount);
			BinGroup<SAMString> bg = new BinGroup<SAMString>(0L,  dict.getSequenceLength(seqName), binCount, binFactory);
			binGroups[dict.getSequenceIndex(seqName)] = bg;
		}
	}
	
	private static int computeBinNumber(long length) {
		int bins=  (int) (length/2e6);
		return Math.max(bins, 1);
	}
	
	public void addRecord(SAMString rec) {
		if (rec.refIndex>-1) { 
			binGroups[rec.refIndex].addItem(rec, (int)rec.pos);
		}
	}
	
	public long size() {
		long total = 0;
		for(int i=0; i<binGroups.length; i++) {
			total += binGroups[i].size();
		}
		return total;
	}
	
	public long memorySize() {
		long total = 0;
		for(int i=0; i<binGroups.length; i++) {
			total += binGroups[i].memSize();
		}
		return total;
	}
	
	/**
	 * Total number of bins 
	 * @return
	 */
	public int getBinTotal() {
		int total = 0;
		for(int i=0; i<binGroups.length; i++) {
			total += binGroups[i].getBinCount();
		}
		return total;
	}
	
	/**
	 * Number of bins with more than zero records in them 
	 * @return
	 */
	public int getUsedBinTotal() {
		int total = 0;
		for(int i=0; i<binGroups.length; i++) {
			total += binGroups[i].getUsedBinCount();
		}
		return total;
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
	
	public BinGroup<SAMString> getBinGroup(int which) {
		return binGroups[which];
	}
	
	public void writeAll(Writeable<SAMString> writer) throws IOException {
		for(int i=0; i<binGroups.length; i++) {
			binGroups[i].writeAll(writer);
		}
	}
}
