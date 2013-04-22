package ss.buffer;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;


public class ConcurrentBuffer<T> {

	//Buffer won't ever contain more than this many items
		final int MAX_BUFFER_SIZE = 65536;

		//The main storage for items
		protected Queue<T> buffer = new ConcurrentLinkedQueue<T>();
		
		protected ProducerTask producer = null;
		protected ConsumerTask consumer = null;
		protected Thread producerThread = null;
		protected Thread consumerThread = null;
		
		/**
		 * Create a new ConcurrentBuffer with the given Producer and Consumer objects. Listener may be null,
		 * if not its processHasFinished() method will be called when both threads have completed.
		 * The 'start()' method must be called to begin both processes
		 * @param prod
		 * @param cons
		 * @param listener
		 */
		public ConcurrentBuffer(Producer<T> prod, Consumer<T> cons) {
			this.producer = new ProducerTask(prod);
			this.consumer = new ConsumerTask(cons);
		}
		
		/**
		 * Start both the producer and consumer threads and block until completion of both. 
		 */
		public void start() {
			producerThread = new Thread(producer);
			consumerThread = new Thread(consumer);
			
			//Add uncaught exception handlers to each thread so we are alerted in something goes awry. 
			producerThread.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {

				@Override
				public void uncaughtException(Thread arg0, Throwable arg1) {
					System.err.println("Reader thread has died: " + arg1.getLocalizedMessage());
					throw new IllegalStateException("Error in reader thread");
				}
				
			});
			
			consumerThread.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {

				@Override
				public void uncaughtException(Thread t, Throwable e) {
					System.err.println("Consumer thread has died: " + e.getLocalizedMessage());
					throw new IllegalStateException("Error in consumer thread");
				}
				
			});
			
			Runtime.getRuntime().addShutdownHook(new Thread() {
				public void run() {
					consumerThread.interrupt();
					producerThread.interrupt();
				}
			});
			
			producerThread.start();
			consumerThread.start();
			
			waitForCompletion();
		}
		
		/**
		 * Block until both the consumer and producer threads are finished.
		 * Then, if the "listener" is non-null, call .processHasFinished() on it
		 */
		private void waitForCompletion() {
			
			while(producerThread.isAlive()) {
				try {
					Thread.sleep(250);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				//Check to make sure consumer is still working, some errors
				//may cause it to die and we'd like to know about it
				if (! consumerThread.isAlive()) {
					throw new IllegalStateException("Buffer consumer thread has died.");
				}
			}
			
			consumer.stopWhenBufferIsEmpty();
			while(consumerThread.isAlive()) {
				try {
					Thread.sleep(250);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}

		class ProducerTask implements Runnable {

			final Producer<T> producer;
			
			ProducerTask(Producer<T> prod) {
				this.producer = prod;
			}
			
			@Override
			public void run() {
				
				while(! producer.isFinishedProducing() && (! Thread.interrupted())) {
					T item = producer.nextItem();
					if (item != null) {
						buffer.add(item);
					}
					
					if (buffer.size() > MAX_BUFFER_SIZE) {
						try {
							Thread.sleep(500);
							System.err.println("Producer is waiting for consumer to catch up....");
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				
			}
			
			public boolean isDone() {
				return producer.isFinishedProducing();
			}
		}
		
		class ConsumerTask implements Runnable {

			final Consumer<T> consumer;
			boolean run = true;
			boolean stopIfEmpty = false;
			
			ConsumerTask(Consumer<T> cons) {
				this.consumer = cons;
			}
			
			@Override
			public void run() {
				
				while(run && (! Thread.interrupted())) {
					T item = buffer.poll();
					if (item != null) {
						consumer.processItem(item);
					}
					else {
						if (stopIfEmpty) {
							break;
						}
					}
				}
				
			}
			
			public void stopWhenBufferIsEmpty() {
				this.stopIfEmpty = true;
			}
			
		}


}
