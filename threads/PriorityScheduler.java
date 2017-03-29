package nachos.threads;

import nachos.machine.*;

import java.util.*;

/**
 * A scheduler that chooses threads based on their priorities.
 * <p>
 * <p>
 * A priority scheduler associates a priority with each thread. The next thread
 * to be dequeued is always a thread with priority no less than any other
 * waiting thread's priority. Like a round-robin scheduler, the thread that is
 * dequeued is, among all the threads of the same (highest) priority, the
 * thread that has been waiting longest.
 * <p>
 * <p>
 * Essentially, a priority scheduler gives access in a round-robin fassion to
 * all the highest-priority threads, and ignores all other threads. This has
 * the potential to
 * starve a thread if there's always a thread waiting with higher priority.
 * <p>
 * <p>
 * A priority scheduler must partially solve the priority inversion problem; in
 * particular, priority must be donated through locks, and through joins.
 */
public class PriorityScheduler extends Scheduler {
    /**
     * Allocate a new priority scheduler.
     */
    public PriorityScheduler() {
    }

    /**
     * Allocate a new priority thread queue.
     *
     * @param transferPriority <tt>true</tt> if this queue should
     *                         transfer priority from waiting threads
     *                         to the owning thread.
     * @return a new priority thread queue.
     */
    public ThreadQueue newThreadQueue(boolean transferPriority) {
        return new PriorityQueue(transferPriority);
    }

    public int getPriority(KThread thread) {
        Lib.assertTrue(Machine.interrupt().disabled());

        return getThreadState(thread).getPriority();
    }

    public int getEffectivePriority(KThread thread) {
        Lib.assertTrue(Machine.interrupt().disabled());

        return getThreadState(thread).getEffectivePriority();
    }

    public void setPriority(KThread thread, int priority) {
        Lib.assertTrue(Machine.interrupt().disabled());

        Lib.assertTrue(priority >= priorityMinimum &&
                priority <= priorityMaximum);

        getThreadState(thread).setPriority(priority);
    }

    public boolean increasePriority() {
        boolean intStatus = Machine.interrupt().disable();

        KThread thread = KThread.currentThread();

        int priority = getPriority(thread);
        if (priority == priorityMaximum)
            return false;

        setPriority(thread, priority + 1);

        Machine.interrupt().restore(intStatus);
        return true;
    }

    public boolean decreasePriority() {
        boolean intStatus = Machine.interrupt().disable();

        KThread thread = KThread.currentThread();

        int priority = getPriority(thread);
        if (priority == priorityMinimum)
            return false;

        setPriority(thread, priority - 1);

        Machine.interrupt().restore(intStatus);
        return true;
    }

    /**
     * The default priority for a new thread. Do not change this value.
     */
    public static final int priorityDefault = 1;
    /**
     * The minimum priority that a thread can have. Do not change this value.
     */
    public static final int priorityMinimum = 0;
    /**
     * The maximum priority that a thread can have. Do not change this value.
     */
    public static final int priorityMaximum = 7;

    /**
     * Return the scheduling state of the specified thread.
     *
     * @param thread the thread whose scheduling state to return.
     * @return the scheduling state of the specified thread.
     */
    protected ThreadState getThreadState(KThread thread) {
        if (thread.schedulingState == null)
            thread.schedulingState = new ThreadState(thread);

        return (ThreadState) thread.schedulingState;
    }

    /**
     * A <tt>ThreadQueue</tt> that sorts threads by priority.
     */
    protected class PriorityQueue extends ThreadQueue {
        public class QueueEntry {
            public KThread thread;
            public int timestamp;

            QueueEntry(KThread thread, int timestamp) {
                this.thread = thread;
                this.timestamp = timestamp;
            }
        }

        PriorityQueue(boolean transferPriority) {
            this.transferPriority = transferPriority;
            Q = new java.util.PriorityQueue<>(new Comparator<QueueEntry>() {
                @Override
                public int compare(QueueEntry e1, QueueEntry e2) {
                    ThreadState ts1 = getThreadState(e1.thread);
                    ThreadState ts2 = getThreadState(e2.thread);
                    int p1 = ts1.getEffectivePriority(), p2 = ts2.getEffectivePriority();
                    if (p1 != p2)
                        return p2 - p1;
                    return e1.timestamp - e2.timestamp;
                }
            });
        }

        public void waitForAccess(KThread thread) {
            Lib.assertTrue(Machine.interrupt().disabled());
            if (transferPriority)
                getThreadState(thread).waitForAccess(this);
            Q.add(new QueueEntry(thread, timestamp++));
        }

        public void acquire(KThread thread) {
            Lib.assertTrue(Machine.interrupt().disabled());
            Lib.assertTrue(Q.isEmpty());
            if (transferPriority)
                getThreadState(thread).acquire(this);
            holder = thread;
        }

        public KThread nextThread() {
            Lib.assertTrue(Machine.interrupt().disabled());
            if (!Q.isEmpty()) {
                if (transferPriority && holder != null)
                    getThreadState(holder).lostHolder(this);
                KThread thread = Q.poll().thread;
                if (transferPriority)
                    getThreadState(thread).beHolder(this);
                holder = thread;
                return thread;
            }
            holder = null;
            return null;
        }

        /**
         * Return the next thread that <tt>nextThread()</tt> would return,
         * without modifying the state of this queue.
         *
         * @return the next thread that <tt>nextThread()</tt> would
         * return.
         */
        protected ThreadState pickNextThread() {
            if (!Q.isEmpty())
                return getThreadState(Q.peek().thread);
            return null;
        }

        public void print() {
            Lib.assertTrue(Machine.interrupt().disabled());
            // implement me (if you want)
            for (QueueEntry e : Q) {
                System.out.print("(" + e.thread.getName() + "," + getPriority(e.thread) + ',' + e.timestamp + ") ");
            }
            System.out.println();
        }

        /**
         * <tt>true</tt> if this queue should transfer priority from waiting
         * threads to the owning thread.
         */
        public boolean transferPriority;
        public java.util.PriorityQueue<QueueEntry> Q;
        public KThread holder;
        public int timestamp = 0;
    }

    /**
     * The scheduling state of a thread. This should include the thread's
     * priority, its effective priority, any objects it owns, and the queue
     * it's waiting for, if any.
     *
     * @see nachos.threads.KThread#schedulingState
     */
    protected class ThreadState {
        /**
         * Allocate a new <tt>ThreadState</tt> object and associate it with the
         * specified thread.
         *
         * @param thread the thread this state belongs to.
         */
        public ThreadState(KThread thread) {
            this.thread = thread;
            modified = true;
            donor = new HashMap<KThread, Integer>();
            recipient = new HashMap<KThread, Integer>();

            setPriority(priorityDefault);
        }

        /**
         * Return the priority of the associated thread.
         *
         * @return the priority of the associated thread.
         */
        public int getPriority() {
            return priority;
        }

        /**
         * Return the effective priority of the associated thread.
         *
         * @return the effective priority of the associated thread.
         */
        public int getEffectivePriority() {
            // implement me
            if (!modified)
                return cachedEffectivePriority;
            cachedEffectivePriority = priority;
            for (KThread t : donor.keySet()) {
                int tmp = getThreadState(t).getEffectivePriority();
                if (tmp > cachedEffectivePriority)
                    cachedEffectivePriority = tmp;
            }
            modified = false;
            return cachedEffectivePriority;
        }

        /**
         * Set the priority of the associated thread to the specified value.
         *
         * @param priority the new priority.
         */
        public void setPriority(int priority) {
            if (this.priority == priority && !modified)
                return;

            this.priority = priority;
            this.modified = true;

            for (KThread t : recipient.keySet()) {
                ThreadState ts = getThreadState(t);
                ts.modified = true;
                ts.setPriority(ts.getPriority());
            }
        }

        /**
         * Called when <tt>waitForAccess(thread)</tt> (where <tt>thread</tt> is
         * the associated thread) is invoked on the specified priority queue.
         * The associated thread is therefore waiting for access to the
         * resource guarded by <tt>waitQueue</tt>. This method is only called
         * if the associated thread cannot immediately obtain access.
         *
         * @param waitQueue the queue that the associated thread is
         *                  now waiting on.
         * @see nachos.threads.ThreadQueue#waitForAccess
         */
        public void waitForAccess(PriorityQueue waitQueue) {
            // implement me
            KThread recipientThread = waitQueue.holder;
            ThreadState recipientThreadState = getThreadState(recipientThread);

            inc(recipientThreadState.donor, thread);
            inc(recipient, recipientThread);

            recipientThreadState.modified = true;
        }

        /**
         * Called when the associated thread has acquired access to whatever is
         * guarded by <tt>waitQueue</tt>. This can occur either as a result of
         * <tt>acquire(thread)</tt> being invoked on <tt>waitQueue</tt> (where
         * <tt>thread</tt> is the associated thread), or as a result of
         * <tt>nextThread()</tt> being invoked on <tt>waitQueue</tt>.
         *
         * @see nachos.threads.ThreadQueue#acquire
         * @see nachos.threads.ThreadQueue#nextThread
         */
        public void acquire(PriorityQueue waitQueue) {
            // implement me
        }

        public void beHolder(PriorityQueue waitQueue) {
            for (PriorityQueue.QueueEntry e : waitQueue.Q) {
                KThread donorThread = e.thread;
                ThreadState donorThreadState = getThreadState(donorThread);

                inc(donor, donorThread);
                inc(donorThreadState.recipient, thread);
            }
            modified = true;
        }

        public void lostHolder(PriorityQueue waitQueue) {
            for (PriorityQueue.QueueEntry e : waitQueue.Q) {
                KThread donorThread = e.thread;
                ThreadState donorThreadState = getThreadState(donorThread);

                dec(donor, donorThread);
                dec(donorThreadState.recipient, thread);
            }
        }

        /**
         * The thread with which this object is associated.
         */
        protected KThread thread;
        /**
         * The priority of the associated thread.
         */
        protected int priority;
        protected int cachedEffectivePriority;
        protected HashMap<KThread, Integer> donor;
        protected HashMap<KThread, Integer> recipient;
        protected boolean modified;

        private void inc(Map<KThread, Integer> map, KThread t) {
            if (map.containsKey(t))
                map.put(t, map.get(t) + 1);
            else
                map.put(t, 1);
        }

        private void dec(Map<KThread, Integer> map, KThread t) {
            map.put(t, map.get(t) - 1);
            if (map.get(t) == 0)
                map.remove(t);
        }
    }
}
