package ss;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import ss.bin.Bin;

public class MultiSorter<T> {

	private final ExecutorService exec;
	
	public MultiSorter(ExecutorService exec) {
		this.exec = exec;
	}
	
	public synchronized void add(Bin<T> binToSort) {
		exec.execute(new SortJob<T>(binToSort));
	}
	
	/**
	 * Blocks until all sorting tasks have completed 
	 */
	public void waitForCompletion() {
		try {
			exec.shutdown();
			exec.awaitTermination(10, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			//Probably no big deal
		}
	}
	
	static class SortJob<T> implements Runnable {

		final Bin<T> binToSort;
		
		SortJob(Bin<T> bin) {
			this.binToSort = bin;
		}
		
		@Override
		public void run() {
			binToSort.sortAll();
		}
		
	}
}
