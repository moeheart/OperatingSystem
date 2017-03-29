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
  listenerNumber = 0;
  lock = new Lock();
  waitForComCV = new Condition2(lock);
  waitForSpeakCV = new Condition2(lock);
  waitForListenCV = new Condition2(lock);
    }

    /**
     * Wait for a thread to listen through this communicator, and then transfer
     * <i>word</i> to the listener.
     * <p/>
     * <p/>
     * Does not return until this thread is paired up with a listening thread.
     * Exactly one listener should receive <i>word</i>.
     *
     * @param    word    the integer to transfer.
     */

    public void speak(int word) {
     // acquire lock first
  lock.acquire();
  // if spoken already, not allow other speakers
  while(afterSpeak) {
   waitForSpeakCV.sleep();
  }

  // change the flag
  afterSpeak = true;
     this.word = word;
  
  // wait for some listener to communicate
  while(listenerNumber == 0) {
   waitForComCV.sleep();
  }
 
  // wake up listener 
  waitForListenCV.wake();
  // wait for listener finish
  waitForComCV.sleep();
  // initialize the flag
  afterSpeak = false;
  // allow other speakers
  waitForSpeakCV.wake();
  lock.release();
    }

    /**
     * Wait for a thread to speak through this communicator, and then return
     * the <i>word</i> that thread passed to <tt>speak()</tt>.
     *
     * @return the integer transferred.
     */

    public int listen() {
     // acquire lock first
        lock.acquire();
        // increase the number
  listenerNumber++; 

  // HIGHLIGHT: 
  if(listenerNumber == 1 && afterSpeak) {
   waitForComCV.wake();
  } 
  // not allow other listeners
  waitForListenCV.sleep();
  // tell speaker i'm finished
  waitForComCV.wake();
  // reducre the number
  listenerNumber--;
  // HIGHLIGHT: avoid from bug caused by context-switching
        int returnWord = this.word;
  lock.release();
  return returnWord;
    }

    // flag of whether already spoken
    private boolean afterSpeak;
    // number of listener waiting in queue
    private int listenerNumber;
    // word
    private int word;
    // lock
    private Lock lock;
    // condition variable of waiting for making pair
    private Condition2 waitForComCV;
    // condition variable of waiting for speaking
    private Condition2 waitForSpeakCV;
    // condition variable of waiting for listening
    private Condition2 waitForListenCV;
}