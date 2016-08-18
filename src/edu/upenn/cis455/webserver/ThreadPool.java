package edu.upenn.cis455.webserver;

import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This class implements the Thread Pool 
 * Reference source: http://tutorials.jenkov.com/java-concurrency/thread-pools.html
 * @author Yibang Chen
 *
 */
public class ThreadPool {
	
	private BlockingQueue socketQueue;
	private static List<ThreadWorker> threadList = new ArrayList<ThreadWorker>();
	private static boolean isTerminated = false;
	private static ServerLogger logger;
	
	/**
	 * The constructor:
	 * 1.	Create a empty blocking queue
	 * 2.	Create threads and add them to threadList, an ArrayList
	 * 3.	Start threads
	 * @param threadCount
	 * @param taskCapacity
	 */
	public ThreadPool (int threadCount, int taskCapacity) {
		logger = ServerLogger.getLogger(HttpServer.getLogFileName());
		socketQueue = new BlockingQueue(taskCapacity);
		ThreadWorker workerThread; //each is a thread
		
		for (int i = 0; i < threadCount; i++) {
			workerThread = new ThreadWorker(socketQueue);
			threadList.add(workerThread);
		}
		
		for (ThreadWorker thread : threadList) {
			thread.start();
		}
	}
	
	/**
	 * Add a new socket to socketQueue
	 * @param s
	 */
	public void addToQueue(Socket s) {
		try {
			socketQueue.enqueue(s);
		} catch (InterruptedException e) {
			logger.writeFile(e.toString());
		}
	}
	
	/**
	 * This method returns thread status
	 * It is called in a worker thread, so set to static
	 * @return the status of *all* threads in the pool
	 */
	public static HashMap<String, String> getThreadStatus() {
		HashMap<String, String> threadStatus = new HashMap<String, String>();
		for (ThreadWorker thread: threadList) {
			if (thread.getState() == Thread.State.WAITING) {
				threadStatus.put(Long.toString(thread.getId()), thread.getState().toString());
			} else {
				threadStatus.put(Long.toString(thread.getId()), thread.getCurrentDirectory());
			}
		}
		return threadStatus;
	}
	
	public synchronized void execute(Runnable task) throws Exception  {
		if (isTerminated) throw
			new IllegalStateException("ThreadPool is terminated");
		this.socketQueue.enqueue(task);
	}
	
	/**
	 * This method terminates *all* threads in the threadList
	 */
	public static synchronized void terminate() {
		isTerminated = true;
		for (ThreadWorker thread : threadList) {
			thread.terminate();
		}
	}
}
