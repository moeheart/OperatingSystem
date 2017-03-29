package nachos.threads;

import nachos.machine.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Uses the hardware timer to provide preemption, and to allow threads to sleep
 * until a certain time.
 */
public class Alarm {
    /**
     * Allocate a new Alarm. Set the machine's timer interrupt handler to this
     * alarm's callback.
     * <p>
     * <p><b>Note</b>: Nachos will not function correctly with more than one
     * alarm.
     */
    public Alarm() {
        entryList = new LinkedList<>();
		System.out.println("Alarm created!");
        Machine.timer().setInterruptHandler(new Runnable() {
            public void run() {
                timerInterrupt();
            }
        });
    }

    /**
     * The timer interrupt handler. This is called by the machine's timer
     * periodically (approximately every 500 clock ticks). Causes the current
     * thread to yield, forcing a context switch if there is another thread
     * that should be run.
     */
    public void timerInterrupt() {
        long currentTime = Machine.timer().getTime();
		//System.out.println("Timer run");
		//System.out.println(entryList.size());
        for(Entry e : entryList){
		//	System.out.println("find");
            if(currentTime >= e.wakeTime){
                e.thread.ready();
                entryList.remove(e);
				break;
            }
        }
    }

    /**
     * Put the current thread to sleep for at least <i>x</i> ticks,
     * waking it up in the timer interrupt handler. The thread must be
     * woken up (placed in the scheduler ready set) during the first timer
     * interrupt where
     * <p>
     * <p><blockquote>
     * (current time) >= (WaitUntil called time)+(x)
     * </blockquote>
     *
     * @param    x    the minimum number of clock ticks to wait.
     * @see    nachos.machine.Timer#getTime()
     */
    public void waitUntil(long x) {
        // for now, cheat just to get something working (busy waiting is bad)
	//	System.out.println("wait...");
        entryList.add(new Entry(KThread.currentThread(), Machine.timer().getTime() + x));
	//	System.out.println(entryList.size());
        boolean intStatus = Machine.interrupt().disable();
        KThread.currentThread().sleep();
        Machine.interrupt().restore(intStatus);
    }

    static public class Entry{
        public KThread thread;
        public long wakeTime;

        public Entry(KThread thread, long wakeTime){
            this.thread = thread;
            this.wakeTime = wakeTime;
        }
    }

    private List<Entry> entryList;
}
