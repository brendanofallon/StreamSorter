package ss;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.concurrent.Executors;

import javax.swing.Timer;

import ss.bin.Bin;
import ss.bin.BinFactory;
import ss.bin.BinGroup;
import ss.bin.SAMStringBinFactory;
import ss.buffer.ConcurrentBuffer;
import ss.sam.SAMHeader;
import ss.sam.SAMString;
import ss.sam.SAMStringConsumer;
import ss.sam.SAMStringProducer;
import ss.sam.SAMStringWriter;



public class StreamSorter {

	final ConcurrentBuffer<SAMString> buffer;

	private MultiBinGroup bins;
	private OutputStream outputStream;
	private MultiSorter<SAMString> sortingQueue;
	private BinFactory<SAMString> binFactory;
	private SAMHeader header = null;
	public static boolean verbose = true;
	
	public StreamSorter(File inputAln, File outputFile) throws IOException {
		this(new FileInputStream(inputAln), new FileOutputStream(outputFile));
	}
	
	public StreamSorter(File inputAln, OutputStream outStream) throws IOException {
		this(new FileInputStream(inputAln), outStream);
	}
	
	public StreamSorter(InputStream inputAln, OutputStream outStream) throws IOException {
		this.outputStream = outStream;
		BufferedReader samReader = new BufferedReader(new InputStreamReader(inputAln));
		header = new SAMHeader(samReader);
		SAMReferenceDictionary dict = header.getDictionary();
		binFactory = new SAMStringBinFactory();
		bins = new MultiBinGroup(dict, binFactory);
		buffer = new ConcurrentBuffer<SAMString>(new SAMStringProducer(dict, samReader), new SAMStringConsumer(bins));
		sortingQueue = new MultiSorter<SAMString>( Executors.newFixedThreadPool(4) );
	}
	
	public void startSorting() {
		Date begin = new Date();
		
		Timer monitor = null;
		
		if (StreamSorter.verbose) {
			monitor = new javax.swing.Timer(1000, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					reportProgress();
				}
			});
			monitor.setDelay(1000);
			monitor.start();
		}
		
		buffer.start(); //Read all input and put it all in 'bins'
		
		Date readTime = new Date();
		
		int jobsAdded = 0;
		if (verbose) {
			System.err.println("Done reading records, now sorting");
		}
		for(int i=0; i<bins.getBinGroupCount(); i++) {
			BinGroup<SAMString> bg = bins.getBinGroup(i);
			for(int j=0; j<bg.getBinCount(); j++) {
				Bin<SAMString> bin = bg.getBin(j);
				if (bin != null && (! bin.isSorted())) {
					sortingQueue.add(bin);
					jobsAdded++;
				}
			}
		}
		
		
		//Wait for sorting to complete
		sortingQueue.waitForCompletion();
		
		if (monitor != null) {
			monitor.stop();
		}
		
		Date sortTime = new Date();
		
		if (verbose) {
			System.err.println("Done sorting, writing all to output");
		}
		
		//Emit everything to some destination	
		try {
			BufferedWriter headerWriter = new BufferedWriter( new OutputStreamWriter(outputStream));
	
			header.writeHeader(headerWriter);
			
			headerWriter.flush();
			
			Writeable<SAMString> writer = new SAMStringWriter(outputStream);
			
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
	
	protected void reportProgress() {
		double memFrac = (double)bins.memorySize() / (double)bins.size();
		System.err.println("Total records: " + bins.size() + " % mem:" + ("" + memFrac*100.0).substring(0,5) + " used bins: " + bins.getUsedBinTotal());
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		//args = new String[]{"test1M.sam", "debugout.bam" };
		
		Date begin = new Date();
		StreamSorter ss;
		try {
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
		}
		catch (Exception ex) {
			System.err.println("Error: " + ex.getLocalizedMessage());
			ex.printStackTrace();
		}

		Date end = new Date();
		double elapsedSecs = (end.getTime() - begin.getTime())/1000.0;
	}
}

