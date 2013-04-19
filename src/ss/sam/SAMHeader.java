package ss.sam;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ss.SAMReferenceDictionary;

/**
 * Stores the header of a SAM file most so it can write to back to the ouput when we're done sorting
 * @author brendan
 *
 */
public class SAMHeader {

	List<String> headerLines = new ArrayList<String>();
	
	public SAMHeader(BufferedReader reader) throws IOException {
		String line = reader.readLine();
		while(line != null && line.startsWith("@")) {
			headerLines.add(line);
			line = reader.readLine();
		}
	}
	
	public SAMReferenceDictionary getDictionary() {
		Map<String, DictEntry> dict = new HashMap<String, DictEntry>();
		
		int index = 0;
		for(String line : headerLines) {
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
		}
		
		return new SAMReferenceDictionary(dict);
	}

	public void writeHeader(Writer output) throws IOException {
		for(String line : headerLines) {
			output.write(line + "\n");
		}
	}
}
