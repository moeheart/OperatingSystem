package nachos.threads;

import nachos.machine.*;

/**
 * An implementation of condition variables that disables interrupt()s for
 * synchronization.
 *
 * <p>
 * You must implement this.
 *
 * @see	nachos.threads.Condition
 */
public class Condition2 {
    /**
     * Allocate a new condition variable.
     *
     * @param	conditionLock	the lock associated with this condition
     *				variable. The current thread must hold this
     *				lock whenever it uses <tt>sleep()</tt>,
     *				<tt>wake()</tt>, or <tt>wakeAll()</tt>.
     */
    public Condition2(Lock conditionLock) {
	System.out.println("INIT2");
	this.conditionLock = conditionLock;
	System.out.println("INIT3");
	this.waitQueue = ThreadedKernel.scheduler.newThreadQueue(false);
	System.out.println("INIT4");
    }

    /**
     * Atomically release the associated lock and go to sleep on this condition
     * variable until another thread wakes it using <tt>wake()</tt>. The
     * current thread must hold the associated lock. The thread will
     * automatically reacquire the lock before <tt>sleep()</tt> returns.
     */
    public void sleep() {
	Lib.assertTrue(conditionLock.isHeldByCurrentThread());

	boolean intStatus = Machine.interrupt().disable();
	waitQueue.waitForAccess(KThread.currentThread());
	conditionLock.release();
	KThread.sleep();
	conditionLock.acquire();
	Machine.interrupt().restore(intStatus);
    }

    /**
     * Wake up at most one thread sleeping on this condition variable. The
     * current thread must hold the associated lock.
     */
    public void wake() {
	Lib.assertTrue(conditionLock.isHeldByCurrentThread());
	boolean intStatus = Machine.interrupt().disable();
	KThread nextThread = waitQueue.nextThread();
    if (nextThread != null) {
        nextThread.ready();
	}        

    Machine.interrupt().restore(intStatus);   
    }

    /**
     * Wake up all threads sleeping on this condition variable. The current
     * thread must hold the associated lock.
     */
    public void wakeAll() {
	Lib.assertTrue(conditionLock.isHeldByCurrentThread());
	boolean intStatus = Machine.interrupt().disable();
	KThread nextThread = waitQueue.nextThread();
    while (nextThread != null) {
        nextThread.ready();
        nextThread = waitQueue.nextThread();
    } 
	Machine.interrupt().restore(intStatus);   
    }

    public Lock conditionLock;
    private ThreadQueue waitQueue;
	
	public static KThread th1 = new KThread();
	public static Condition2 cond = new Condition2(new Lock());
	public static void selfTest() {
		Lib.debug(dbgThread, "Enter KThread.selfTest");
		System.out.println("Start Condition2 selftest...");
		th1 = new KThread(new KThread.PingTest2(cond, 1));
		th1.setName("forked thread").fork();
		new KThread(new KThread.PingTest2(cond, 2)).setName("forked thread2").fork();
		System.out.println("33333");
		
		new KThread.PingTest2(cond, 0).run();
		System.out.println("22222");
    }
	
	private static final char dbgThread = 't';
}
