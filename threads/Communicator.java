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
     afterSpeak = false;
     listenernum = 0;
     lock = new Lock();
	 System.out.println("INIT!");
     waitforspeakCV = new Condition2(lock);
     waitforlistenCV = new Condition2(lock);
     waitforcomCV = new Condition2(lock);
     waitforfinCV = new Condition2(lock);
    }

    /**
     * Wait for a thread to listen through this communicator, and then transfer
     * <i>word</i> to the listener.
     *
     * <p>
     * Does not return until this thread is paired up with a listening thread.
     * Exactly one listener should receive <i>word</i>.
     *
     * @param word the integer to transfer.
     */
    public void speak(int word) {
     // acquire lock first.
     lock.acquire();
     // If already speak, just wait.
     while (afterSpeak) {
      waitforspeakCV.sleep();
     }

     afterSpeak = true;
     this.word = word;
     // If nobody listen, just wait.
     while (listenernum == 0) {
      waitforcomCV.sleep();
     }
     // ensure someone is listening.
     Lib.assertTrue (listenernum > 0);
     // reduce the in-pair listener.
     listenernum -= 1;
     // listener can listen now.
     waitforlistenCV.wake();
     // can speak now
     waitforspeakCV.wake();
     // release the lock
    }

    /**
     * Wait for a thread to speak through this communicator, and then return
     * the <i>word</i> that thread passed to <tt>speak()</tt>.
     *
     * @return the integer transferred.
     */    
    public int listen() {
     // acquire lock first.
  lock.acquire();
  listenernum += 1;

  while (!afterSpeak) {
   waitforlistenCV.sleep();
  }

  // initialize the flag
     afterSpeak = false;
     waitforcomCV.wake();
  int theword = this.word;
  lock.release();
  return theword;
    }
	
	static public Communicator c = new Communicator();
	public static void selfTest() {
		Lib.debug(dbgThread, "Enter KThread.selfTest");
		System.out.println("Start Communicator selftest...");
		new KThread(new KThread.CommTest(c,1)).setName("forked thread").fork();
		new KThread(new KThread.CommTest(c,2)).setName("forked thread2").fork();
		System.out.println("33333");
		new KThread.CommTest(c,0).run();
		System.out.println("22222");
    }
	
	public static void selfTestCplx() {
	//	static public Communicator c = new Communicator();
		Lib.debug(dbgThread, "Enter KThread.selfTest");
		System.out.println("Start Communicator selftest (Complex mode)...");
		for (int i = 1; i < 10; i++) {
			new KThread(new KThread.CommTestCplx(c,i)).setName("forked thread"+i).fork();
		}
		new KThread.CommTestCplx(c,0).run();
    }
	
	private static final char dbgThread = 'c';
   private int word;
    // the flag of whether speaker has spoken.
   private boolean afterSpeak;
   // the number of listener waiting for listening.
   private int listenernum;
   private Lock lock;
   // the condition variable of waiting speakers.
   private Condition2 waitforspeakCV;
   // the condition variable of waiting listeners.
   private Condition2 waitforlistenCV;
   // the condition variable of thread that waiting for a listener to listen the speaking, namely, waiting for communication.
   private Condition2 waitforcomCV;
   // the condition variable of waiting for finish.
   private Condition2 waitforfinCV;
}
