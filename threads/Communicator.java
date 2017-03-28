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
	
	public static void selfTest() {
		Lib.debug(dbgThread, "Enter KThread.selfTest");
		System.out.println("Start Communicator selftest...");
		new KThread(new KThread.CommTest(1)).setName("forked thread").fork();
		new KThread(new KThread.CommTest(2)).setName("forked thread2").fork();
		System.out.println("33333");
		new KThread.CommTest(0).run();
		System.out.println("22222");
    }
	
	private static final char dbgThread = 'c';
}
