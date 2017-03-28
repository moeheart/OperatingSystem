package nachos.threads;

import nachos.machine.*;

/**
 * A <i>communicator</i> allows threads to synchronously exchange 32-bit
 * messages. Multiple threads can be waiting to <i>speak</i>,
 * and multiple threads can be waiting to <i>listen</i>. But there should never
 * be a time when both a speaker and a listener are waiting, because the two
 * threads can be paired off at this point.
 */
public class Communicator {
    /**
     * Allocate a new communicator.
     */
    public Communicator() {
    }

    /**
     * Wait for a thread to listen through this communicator, and then transfer
     * <i>word</i> to the listener.
     *
     * <p>
     * Does not return until this thread is paired up with a listening thread.
     * Exactly one listener should receive <i>word</i>.
     *
     * @param	word	the integer to transfer.
     */
    public void speak(int word) {
    }

    /**
     * Wait for a thread to speak through this communicator, and then return
     * the <i>word</i> that thread passed to <tt>speak()</tt>.
     *
     * @return	the integer transferred.
     */    
    public int listen() {
	return 0;
    }
	
	public static class CommTest implements Runnable {
		CommTestTest1(int which) {
			this.which = which;
		}
		public void run() {
			int[][10] a = new int[][10]{{0, 0, 1, 0, 0, 0, 0, 0, 0},{0, 0, 0, 0, 2, 0, 2, 0, 0, 0},{0, 0, 0, 0, 0, 0, 0, 0, 1, 0}};
			for (int i=0; i<10; i++) {
			if (a[which][i] == 1) {
				System.out.println("Thread " + which + "ready to listen...");
				int res = listen();
				System.out.println("Thread " + which + "listened result: " + res);
			}
			if (a[which][i] == 2) {
				int text = 233;
				System.out.println("Thread " + which + "ready to speak" + text);
				speak(text);
				System.out.println("Thread " + which + "speak complete!");
			}
			System.out.println("3*** thread " + which + " looped "
					   + i + " times");
			currentThread.yield();
			}
		}
		private int which;
    }
	
	public static void selfTest() {
		Lib.debug(dbgThread, "Enter KThread.selfTest");
		System.out.println("Start Communicator selftest...");
		th1 = new KThread(new KThread.PingTest2(cond, 1));
		th1.setName("forked thread").fork();
		new KThread(new KThread.PingTest2(cond, 2)).setName("forked thread2").fork();
		System.out.println("33333");
		
		new KThread.PingTest2(cond, 0).run();
		System.out.println("22222");
    }
	
	
	
	private static final char dbgThread = 'c';
}
