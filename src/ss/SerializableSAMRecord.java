package ss;

import java.io.Serializable;

import net.sf.samtools.SAMRecord;

public class SerializableSAMRecord implements Serializable {

	private static final long serialVersionUID = -8813366526571650773L;
	public final SAMRecord rec;
	
	public SerializableSAMRecord(SAMRecord rec) {
		this.rec = rec;
	}
}
