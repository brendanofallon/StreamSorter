package ss.sam;

import ss.MultiBinGroup;
import ss.buffer.Consumer;

/**
 * Take records from the end of the concurrentbuffer and 'process' them by adding them to a bin. 
 * @author brendan
 *
 */
public class SAMStringConsumer implements Consumer<SAMString> {

	final MultiBinGroup bins;
	private int recordsProcessed = 0;
	
	public SAMStringConsumer(MultiBinGroup bins) {
		this.bins = bins;
	}
	
	@Override
	public void processItem(SAMString item) {
		recordsProcessed++;
		bins.addRecord(item);
	}

}
