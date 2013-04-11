package ss;

import ss.buffer.Consumer;
import net.sf.samtools.SAMRecord;



public class SAMRecordConsumer implements Consumer<SAMRecord> {

	final MultiBinGroup bins;
	private int recordsProcessed = 0;
	
	public SAMRecordConsumer(MultiBinGroup bins) {
		this.bins = bins;
	}
	
	@Override
	public void processItem(SAMRecord item) {
		recordsProcessed++;
		if (recordsProcessed % 100000 == 0) {
			System.err.println("Consumer has processed " + recordsProcessed );
		}
		bins.addRecord(item);
	}

}
