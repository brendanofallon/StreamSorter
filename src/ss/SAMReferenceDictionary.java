package ss;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import ss.sam.DictEntry;

/**
 * Reader and container for information about the reference sequences from a SAM header
 * @author brendan
 *
 */
public class SAMReferenceDictionary {

	private Map<String, DictEntry> dict = new HashMap<String, DictEntry>();

	public SAMReferenceDictionary(BufferedReader reader) throws IOException {
		readHeader(reader);
	}
	
	public SAMReferenceDictionary(File samFile) throws FileNotFoundException, IOException {
		readHeader(new BufferedReader(new FileReader(samFile)));
	}
	
	public SAMReferenceDictionary(Map<String, DictEntry> dict) {
		this.dict = dict;
	}
	
	private void readHeader(BufferedReader reader) throws IOException {
		String line = reader.readLine();
		int index = 0;
		while(line != null && line.startsWith("@")) {
			if (line.startsWith("@SQ")) {
				String[] toks = line.split("\t");
				String refName = null;
				Long refLength = null;
				for(int i=0; i<toks.length; i++) {
					
					if (toks[i].startsWith("SN:")) {
						refName = toks[i].replace("SN:", "");
					}
					
					if (toks[i].startsWith("LN:")) {
						refLength = Long.parseLong( toks[i].replace("LN:", ""));
					}
					
					if (refName != null && refLength != null) {
						dict.put(refName, new DictEntry(index, refLength));
						index++;
					}
				}
			}
			line = reader.readLine();
		}
	}
	
	public int getSequenceCount() {
		return dict.size();
	}
	
	public long getSequenceLength(String refName) {
		DictEntry entry = dict.get(refName);
		if (entry == null) {
			return -1;
		}
		else {
			return entry.length;
		}
	}
	
	public int getSequenceIndex(String refName) {
		DictEntry entry = dict.get(refName);
		if (entry == null) {
			return -1;
		}
		else {
			return entry.index;
		}
	}
	
	public Set<String> getSequenceNames() {
		return dict.keySet();
	}
	
	
}
