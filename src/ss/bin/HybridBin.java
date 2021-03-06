package ss.bin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMFileReader.ValidationStringency;
import net.sf.samtools.SAMFileWriter;
import net.sf.samtools.SAMFileWriterFactory;
import net.sf.samtools.SAMRecord;
import ss.SAMWriterContext;
import ss.Writeable;

public class HybridBin extends AbstractSAMRecordBin {

	final int recordMemoryLength = 8192;
	final String tmpDir = System.getProperty("java.io.tmpdir");
	private SAMFileWriter writer = null;
	private SAMFileHeader header = null;
	private SAMFileWriterFactory factory = null;
	private File tmpFile;
	private int itemsAdded = 0; //total records added
	
	private SAMRecord[] records = null; //dont allocate until we need it
	private int arrayUsed = 0;
	
	private int refIndex = -1; //Used to ensure that we dont accept records from multiple references
	
	public HybridBin(long start, long end, SAMWriterContext writerContext) {
		super(start, end);
		this.factory = writerContext.factory;
		this.header = writerContext.header;
	}

	@Override
	public void add(SAMRecord item) {
		if (records == null) {
			 records = new SAMRecord[recordMemoryLength];
		}
		
		if (arrayUsed < records.length) {
			records[arrayUsed] = item;
			arrayUsed++;
		}
		else {
			if (writer == null) {
				String randomBits = randomStr(12);
				tmpFile = new File(tmpDir + System.getProperty("file.separator") + "streamsorter."+ randomBits + ".bam");
				tmpFile.deleteOnExit();
				writer = factory.makeBAMWriter(header, false, tmpFile,1);
			}
			writer.addAlignment(item);
		}
		
		if (refIndex < 0) {
			refIndex = item.getReferenceIndex();
		}
		if (item.getReferenceIndex() != refIndex) {
			throw new IllegalStateException("Cannot accept record from reference #" + item.getReferenceIndex() + " initial ref index: " + refIndex);
		}
		
		sorted = false;
		itemsAdded++;
	}

	@Override
	public int size() {
		return itemsAdded;
	}

	@Override
	public void sortAll() {
		if (itemsAdded < 2) {
			sorted = true;
			return;
		}
		
		if (itemsAdded <= records.length) {
			//Sweet, everything is in memory. Sort it...
			Arrays.sort(records, 0, arrayUsed, new SAMRecordComparator());
			sorted = true;
		}
		else {
			try {
				//Make sure no more writes happen
				if (writer == null) {
					System.err.println("Writer can't be null, array used: " + arrayUsed + " items added: " + itemsAdded);
				}
				writer.close();
				
				//Put items from array in a list
				List<SAMRecord> items = new ArrayList<SAMRecord>(itemsAdded);
				for(int i=0; i<arrayUsed; i++) {
					items.add(records[i]);
				}
				
				//Read everything from the file and add all those items to the list
				items = readToRAM(items);

				//Sort it all
				Collections.sort(items, new SAMRecordComparator());

				tmpFile.delete(); 
				
				//System.err.println("Sorting items reference: " + refIndex + " " + items.get(0).getAlignmentStart() + " - " + items.get(items.size()-1).getAlignmentStart());
				
				//Write records back to memory, spilling to file if needed
				int pos = Integer.MIN_VALUE;
				SAMFileWriter finalWriter = null;
				for(int index=0; index<items.size(); index++) {
					SAMRecord rec = items.get(index);
					
					if (rec.getAlignmentStart() < pos) {
						throw new IllegalArgumentException("Hmm, records in block are not actually sorted in the in-mem area");
					}
					pos = rec.getAlignmentStart();
					
					
					if (index < records.length) {
						records[index] = rec;
					}
					else {
						if (finalWriter == null) {
							finalWriter = factory.makeBAMWriter(header, true, tmpFile,1);			
						}
						finalWriter.addAlignment(rec);	
					}
					
					
				}
				
				if (finalWriter != null) {
					try {
						finalWriter.close();
					}
					catch (Exception ex) {
						System.err.println("Exception closing file " + tmpFile + ": " + ex.getLocalizedMessage());
					}
				}
				sorted = true;
				
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (ClassNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
	
	private List<SAMRecord> readToRAM(List<SAMRecord> items) throws IOException, ClassNotFoundException {

		//Read in all items
		SAMFileReader reader = new SAMFileReader(tmpFile);
		reader.setValidationStringency(ValidationStringency.LENIENT);
		Iterator<SAMRecord> sit = reader.iterator();
		while(sit.hasNext()) {
			items.add(sit.next());
		}
		reader.close();
		return items;
	}
	
	@Override
	public void writeItems(Writeable<SAMRecord> output) throws IOException {

		if (! sorted) {
			throw new IllegalStateException("Bin has not been sorted! items added: " +itemsAdded + " array used: " + arrayUsed);
		}
		
		for(int i=0; i<arrayUsed; i++) {
			output.write(records[i]);
		}

		long prevPos = Integer.MIN_VALUE;
		if (itemsAdded>records.length) {
			SAMFileReader reader = new SAMFileReader(tmpFile);
			Iterator<SAMRecord> sit = reader.iterator();
			while(sit.hasNext()) {
				SAMRecord rec = sit.next();
				if (rec.getAlignmentStart() < prevPos) {
					reader.close();
					throw new IllegalStateException("Block " + start + "-" + end + " is not sorted!");
				}
				output.write(rec);
				
			}
			reader.close();
		}

		if (tmpFile != null) {
			tmpFile.delete();
		}
	}

	private static String randomStr(int length) {
		StringBuilder str = new StringBuilder();
		for(int i=0; i<length; i++) {
			str.append(chars[(int)(Math.random()*chars.length)]);
		}
		return str.toString();
	}
	
	
	
	
	final static char[] chars = new char[]{'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z','1','2','3','4','5','6','7','8','9','0'};
	
}
