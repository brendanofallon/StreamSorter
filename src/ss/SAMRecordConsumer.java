package ss;

import net.sf.samtools.SAMRecord;
import ss.buffer.Consumer;



public class SAMRecordConsumer implements Consumer<SAMRecord> {

	final MultiBinGroup bins;
	private int recordsProcessed = 0;
	
	public SAMRecordConsumer(MultiBinGroup bins) {
		this.bins = bins;
	}
	
	@Override
	public void processItem(SAMRecord item) {
		recordsProcessed++;
		bins.addRecord(item);
	}

}
