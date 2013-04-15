package ss;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.concurrent.Executors;

import net.sf.samtools.SAMFileWriterFactory;
import net.sf.samtools.SAMRecord;
import ss.bin.Bin;
import ss.bin.BinFactory;
import ss.bin.BinGroup;
import ss.bin.SAMRecordBinFactory;
import ss.buffer.ConcurrentBuffer;
import ss.buffer.Consumer;



public class StreamSorter {

	final ConcurrentBuffer<SAMRecord> buffer;
	final SAMReaderProducer reader;
	final Consumer<SAMRecord> consumer;

	private MultiBinGroup bins;
	private OutputStream outputStream;
	private MultiSorter<SAMRecord> sortingQueue;
	private BinFactory<SAMRecord> binFactory;

	public StreamSorter(File inputAln, File outputFile) throws FileNotFoundException {
		this(new FileInputStream(inputAln), new FileOutputStream(outputFile));
	}
	
	public StreamSorter(File inputAln, OutputStream outStream) throws FileNotFoundException {
		this(new FileInputStream(inputAln), outStream);
	}
	
	public StreamSorter(InputStream inputAln, OutputStream outStream) {
		this.outputStream = outStream;
		reader = new SAMReaderProducer(inputAln);
		binFactory = new SAMRecordBinFactory(new SAMWriterContext(new SAMFileWriterFactory(), reader.getHeader() ));
		bins = new MultiBinGroup(reader.getHeader(), binFactory);
		consumer = new SAMRecordConsumer(bins);
		buffer = new ConcurrentBuffer<SAMRecord>(reader, consumer);
		sortingQueue = new MultiSorter<SAMRecord>(Executors.newFixedThreadPool(4));
	}
	
	public void startSorting() {
		Date begin = new Date();
		
		buffer.start(); //
		
		//bins.emitAll();
		Date readTime = new Date();
		
		for(int i=0; i<bins.getBinGroupCount(); i++) {
			BinGroup<SAMRecord> bg = bins.getBinGroup(i);
			for(int j=0; j<bg.getBinCount(); j++) {
				Bin<SAMRecord> bin = bg.getBin(j);
				if (bin != null && (! bin.isSorted()))
					sortingQueue.add(bin);
			}
		}
		
		
		//Wait for sorting to complete
		sortingQueue.waitForCompletion();
		
		//Producer thread will read from input stream and push to the buffer, consumer threads
		//pull records from the buffer and do 
		buffer.start();
		Date sortTime = new Date();
		
		//Emit everything to some destination
		SAMFileWriterFactory factory = new SAMFileWriterFactory();
		SAMRecordWriter writer = new SAMRecordWriter(factory.makeSAMWriter(reader.getHeader(), true, outputStream));
		
		try {
			bins.writeAll(writer);
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Date end = new Date();
		double readSecs = (readTime.getTime() - begin.getTime())/1000.0;
		double sortSecs = (sortTime.getTime() - readTime.getTime())/1000.0;
		double writeTime = (end.getTime() - sortTime.getTime())/1000.0;
		System.err.println("Read seconds : " + readSecs);
		System.err.println("Sort seconds: " + sortSecs);
		System.err.println("Write seconds: " + writeTime);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		//args = new String[]{"test1M.sam", "debugout.bam" };
		
		Date begin = new Date();
		StreamSorter ss;
		if (args.length==0) {
			ss = new StreamSorter(System.in, System.out);
			ss.startSorting();
		}
		else {
			try {
				ss = new StreamSorter(new File(args[0]), System.out);
				ss.startSorting();
			} catch (FileNotFoundException e) {
				System.err.println("Input file : "+ args[0] + " not found");
			}
		}
		
		

		Date end = new Date();
		double elapsedSecs = (end.getTime() - begin.getTime())/1000.0;
	}
}

