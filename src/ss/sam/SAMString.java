package ss.sam;

public class SAMString implements Comparable<SAMString> {

	public final int refIndex;
	public final long pos;
	public final String data;
	
	public SAMString(String data, long pos, int refIndex) {
		this.data = data;
		this.pos = pos;
		this.refIndex = refIndex;
	}

	@Override
	public int compareTo(SAMString o) {
		if (o == this) {
			return 0;
		}
		if (o.refIndex != refIndex) {
			return refIndex - o.refIndex;
		}
		
		return (int)(pos - o.pos);
	}

}
