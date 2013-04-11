package ss;

import ss.buffer.Consumer;
import net.sf.samtools.SAMRecord;

public class TestConsumer implements Consumer<SAMRecord> {

	@Override
	public void processItem(SAMRecord record) {
		System.out.println("Got chr" + record.getReferenceName() + " pos: " + record.getAlignmentStart());
		System.out.flush();
	}



}
