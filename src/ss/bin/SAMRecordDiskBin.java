package ss.bin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMFileWriter;
import net.sf.samtools.SAMFileWriterFactory;
import net.sf.samtools.SAMRecord;

import ss.SAMWriterContext;
import ss.Writeable;

/**
 * A SAMRecord bin that stores all of the records on disk
 * @author brendanofallon
 *
 */
public class SAMRecordDiskBin extends AbstractSAMRecordBin {

	final String tmpDir = System.getProperty("java.io.tmpdir");
	private SAMFileWriter writer = null;
	private SAMFileHeader header = null;
	private SAMFileWriterFactory factory = null;
	private File tmpFile;
	private int itemsAdded = 0;
	
	public SAMRecordDiskBin(long start, long end, SAMWriterContext writerContext) {
		super(start, end);
		this.factory = writerContext.factory;
		this.header = writerContext.header;
	}

	@Override
	public void add(SAMRecord item) {
		if (writer == null) {
			String randomBits = randomStr(8);
			tmpFile = new File(tmpDir + System.getProperty("file.separator") + "streamsorter."+ randomBits + ".bam");
			writer = factory.makeBAMWriter(header, false, tmpFile,1);
		}
		sorted = false;
		writer.addAlignment(item);
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
			writer.close();
			return;
		}
		List<SAMRecord> items;
		try {
			//Make sure no more writes happen
			writer.close();
			
			//Read everything to a big list
			items = readToRAM();
			
			//Sort them all
			Collections.sort(items, new SAMRecordComparator());
			
			//write them back to the file
			writer = factory.makeBAMWriter(header, true, tmpFile, 1);
			for(SAMRecord rec: items) {
				writer.addAlignment(rec);
			}
			writer.close();
			sorted = true;
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	private List<SAMRecord> readToRAM() throws IOException, ClassNotFoundException {
		List<SAMRecord> items = new ArrayList<SAMRecord>(itemsAdded);

		//Read in all items
		SAMFileReader reader = new SAMFileReader(tmpFile);
		Iterator<SAMRecord> sit = reader.iterator();
		while(sit.hasNext()) {
			items.add(sit.next());
		}
		reader.close();
		return items;
	}
	
	@Override
	public void writeItems(Writeable<SAMRecord> output) throws IOException {
		
		if (itemsAdded > 0) {
			SAMFileReader reader = new SAMFileReader(tmpFile);
			Iterator<SAMRecord> sit = reader.iterator();
			while(sit.hasNext()) {
				output.write(sit.next());
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
