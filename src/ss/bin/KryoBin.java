package ss.bin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.sf.samtools.SAMRecord;

import org.objenesis.strategy.StdInstantiatorStrategy;

import ss.SAMWriterContext;
import ss.Writeable;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * This bin type is like HybridBin in that it maintains some records in memory but spills
 * to disk if needed. It uses the Kryo library to serialize SAMRecords when writing to disk
 * @author brendan
 *
 */
public class KryoBin extends AbstractSAMRecordBin {

	final int recordMemoryLength = 1024;
	final String tmpDir = System.getProperty("java.io.tmpdir");
	private Kryo kryo = null;
	private Output kryoOutput = null;
	private Input kryoInput = null;
	private File tmpFile;
	private int itemsAdded = 0; //total records added
	
	private SAMRecord[] records = new SAMRecord[recordMemoryLength];
	private int arrayUsed = 0;
	
	private int refIndex = -1; //Used to ensure that we dont accept records from multiple references
	
	public KryoBin(long start, long end, SAMWriterContext writerContext) {
		super(start, end);
	}

	@Override
	public void add(SAMRecord item) {
		if (arrayUsed < records.length) {
			records[arrayUsed] = item;
			arrayUsed++;
		}
		else {
			if (kryo == null) {
				kryo = new Kryo();
				String randomBits = randomStr(12);
				tmpFile = new File(tmpDir + System.getProperty("file.separator") + "streamsorter."+ randomBits + ".kryo");
				try {
					tmpFile.createNewFile();
				} catch (IOException e1) {
					throw new IllegalArgumentException("Could not create kryo output file: " + tmpFile.getAbsolutePath());
				}
				tmpFile.deleteOnExit();
				try {
					kryoOutput = new Output(new FileOutputStream(tmpFile));
				} catch (FileNotFoundException e) {
					throw new IllegalArgumentException("Could not write kryo output file: " + tmpFile.getAbsolutePath());
				}
			}
			kryo.writeObject(kryoOutput, item);
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
				kryoOutput.close();
				
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
				try {
					kryoOutput = new Output(new FileOutputStream(tmpFile));
				} catch (FileNotFoundException e) {
					throw new IllegalArgumentException("Could not write kryo output file: " + tmpFile.getAbsolutePath());
				}
				
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
						kryo.writeObject(kryoOutput, rec);	
					}		
					
				}
				
				kryoOutput.close();
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
		Input input = new Input(new FileInputStream(tmpFile));
		kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
		while (! input.eof()) {
			SAMRecord rec = kryo.readObject(input, SAMRecord.class);
			items.add(rec);
		}
		input.close();
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
			Input input = new Input(new FileInputStream(tmpFile));
			kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
			while (! input.eof()) {
				SAMRecord rec = kryo.readObject(input, SAMRecord.class);
				output.write(rec);
			}
			input.close();
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
