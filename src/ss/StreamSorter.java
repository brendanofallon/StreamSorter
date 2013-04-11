package ss;


import java.io.File;
import java.io.InputStream;

import net.sf.samtools.SAMRecord;

public class StreamSorter {

	final ConcurrentBuffer<SAMRecord> buffer;
	final SAMReaderProducer reader;
	final Consumer<SAMRecord> consumer;
	
	public StreamSorter(InputStream inputAln, File outputFile) {
		reader = new SAMReaderProducer(inputAln);
		consumer = new TestConsumer();
		buffer = new ConcurrentBuffer<SAMRecord>(reader, consumer);
	}
	
	public void startSorting() {
		
		//Producer thread will read from input stream and push to the buffer, consumer threads
		//pull records from the buffer and do 
		buffer.start();
		
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
//		StreamSorter ss = new StreamSorter(System.in, new File("testout.bam"));
//		ss.startSorting();
		
		int numberOfBins = 414;

		for (numberOfBins = 5; numberOfBins < 10001; numberOfBins++) {
			BinGroup<String> bg = new BinGroup<String>(104, 100037, numberOfBins, new BinFactory<String>() {

				@Override
				public Bin<String> createBin(long start, long end) {
					// TODO Auto-generated method stub
					return null;
				}

			});

			long prevEnd = -1;
			for(int i=0; i<numberOfBins; i++) {
				if (prevEnd > 0 && (bg.startPosForBin(i) != (prevEnd+1))) {
					throw new IllegalStateException("Yikes, not correct!");
				}
				System.out.println("Bin : " + i + " start: " + bg.startPosForBin(i) + " end: " +bg.endPosForBin(i));
				prevEnd = bg.endPosForBin(i);
			}

		}
	}

}
