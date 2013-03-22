package ss;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ConcurrentBuffer<T> {

	//Buffer won't ever contain more than this many items
		final int MAX_BUFFER_SIZE = 16384;

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
				System.out.println("Waiting for producer to die...");
				try {
					Thread.sleep(250);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			consumer.stopWhenBufferIsEmpty();
			System.out.println("Producer is dead, waiting for consumer to die...");
			while(consumerThread.isAlive()) {
				System.out.println("Waiting for consumer to die...");
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
				
				while(! producer.isFinishedProducing()) {
					T item = producer.nextItem();
					if (item != null) {
						buffer.add(item);
					}
					
					if (buffer.size() > MAX_BUFFER_SIZE) {
						try {
							Thread.sleep(500);
							System.out.println("Producer is waiting for consumer to catch up....");
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
				
				while(run) {
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
