package ss.sam;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.StringTokenizer;

import ss.SAMReferenceDictionary;
import ss.buffer.Producer;

public class SAMStringProducer implements Producer<SAMString> {

	public static final String DELIM = "\t"; //Delimiter for fields in both header and data of SAM files
	private BufferedReader reader;
	private boolean finished = false;
	private SAMReferenceDictionary dict;
	
	public SAMStringProducer(SAMReferenceDictionary dict, BufferedReader reader) throws IOException {
		this.dict = dict;
		this.reader = reader;
	}
	
	
	@Override
	public boolean isFinishedProducing() {
		return finished;
	}

	@Override
	public SAMString nextItem() {
		try {
			String data = reader.readLine();
			if (data == null) {
				finished = true;
				return null;
			}
			StringTokenizer tokenizer = new StringTokenizer(data, DELIM);
			tokenizer.nextToken(); //Skip QNAME
			tokenizer.nextToken(); //Skip FLAG
			String refName = tokenizer.nextToken();
			String posStr = tokenizer.nextToken();
			long pos = Long.parseLong(posStr);
			Integer refIndex = dict.getSequenceIndex(refName);
			SAMString item = new SAMString(data, pos, refIndex);
			return item;
		} catch (IOException e) {
		
			e.printStackTrace();
		}
		
		
		return null;
	}

}
