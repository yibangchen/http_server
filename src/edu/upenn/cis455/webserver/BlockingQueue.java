package edu.upenn.cis455.webserver;

import java.util.LinkedList;
import java.util.List;

/**
 * This class is a blocking queue implementation
 * reference source: http://tutorials.jenkov.com/java-concurrency/blocking-queues.html
 * @author Yibang Chen
 *
 */
public class BlockingQueue {
	
	private List<Object> queue = new LinkedList<Object>() ;
	private int size;
	private static ServerLogger logger;
	
	/**
	 * The constructor
	 * @param size
	 * @throws IllegalArgumentException
	 */
	public BlockingQueue(int size) throws IllegalArgumentException{
		this.size = size;
		logger = ServerLogger.getLogger(HttpServer.getLogFileName());
	}
	
	/**
	 * Add a new object to end of queue; wait when full, notify all when empty
	 * @param obj : object to add to queue
	 * @throws InterruptedException
	 */
	public synchronized void enqueue (Object obj) throws InterruptedException {
		while (isFull())
			try {
				wait();
			} catch (InterruptedException e) {	
				logger.writeFile(e.toString());
			}
		
		if (isEmpty())	notifyAll();
		
		queue.add(obj);
	}
	
	/**
	 * Take the first object of the queue out; wait when empty queue
	 * @return first object of the queue
	 * @throws InterruptedException
	 */
	public synchronized Object dequeue() throws InterruptedException {
		while (isEmpty())
			try {
				wait();
			} catch (InterruptedException e) {
				logger.writeFile(e.toString());
			}
		
		if (isFull())	notifyAll();
		
		return queue.remove(0);
	}
	
	/**
	 * Check if queue is empty
	 * @return true if empty, false otherwise
	 */
	private boolean isEmpty() {
		return queue.size() == 0;
	}
	
	/**
	 * Check if queue is full
	 * @return true if full, false otherwise
	 */
	private boolean isFull() {
		return queue.size() == size;
	}
}
