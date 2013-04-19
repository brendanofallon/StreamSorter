package ss.sam;

/**
 * Stores info about reference index and length
 * @author brendan
 *
 */
public class DictEntry {

	public final int index;
	public final long length;

	public DictEntry(int index, long length) {
		this.index = index;
		this.length = length;
	}

}
