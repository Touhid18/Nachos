package nachos.threads;

import nachos.machine.*;

/**
 * An implementation of condition variables that disables interrupt()s for
 * synchronization.
 * 
 * <p>
 * You must implement this.
 * 
 * @see nachos.threads.Condition
 */
public class Condition2 {
	/**
	 * Allocate a new condition variable.
	 * 
	 * @param conditionLock
	 *            the lock associated with this condition variable. The current
	 *            thread must hold this lock whenever it uses <tt>sleep()</tt>,
	 *            <tt>wake()</tt>, or <tt>wakeAll()</tt>.
	 */
	public Condition2(Lock conditionLock) {
		this.conditionLock = conditionLock;
		this.waitingThreadQueue = ThreadedKernel.scheduler.newThreadQueue(true);
	}

	/**
	 * Atomically release the associated lock and go to sleep on this condition
	 * variable until another thread wakes it using <tt>wake()</tt>. The current
	 * thread must hold the associated lock. The thread will automatically
	 * reacquire the lock before <tt>sleep()</tt> returns.
	 */
	public void sleep() {
		/* If the current thread doesn't hold the lock, then abort */
		Lib.assertTrue(conditionLock.isHeldByCurrentThread());

		// FIXME Touhid : edited except conditionLock method calls
		boolean oldInterruptStatus = Machine.interrupt().disable();
		// Add thread to waitingThreads
		waitingThreadQueue.waitForAccess(KThread.currentThread());

		// Release lock
		conditionLock.release();

		// Go to sleep
		KThread.sleep();

		// Get lock upon awakening
		conditionLock.acquire();

		Machine.interrupt().restore(oldInterruptStatus);
	}

	/**
	 * Wake up at most one thread sleeping on this condition variable. The
	 * current thread must hold the associated lock.
	 */
	public void wake() {
		Lib.assertTrue(conditionLock.isHeldByCurrentThread());

		// FIXME edit starts
		boolean oldInterruptStatus = Machine.interrupt().disable();
		KThread thread = waitingThreadQueue.nextThread();
		if (thread != null) {
			thread.ready();
		}
		Machine.interrupt().restore(oldInterruptStatus);
		// Edit ends
	}

	/**
	 * Wake up all threads sleeping on this condition variable. The current
	 * thread must hold the associated lock.
	 */
	public void wakeAll() {
		Lib.assertTrue(conditionLock.isHeldByCurrentThread());

		// FIXME edit starts
		boolean oldInterruptStatus = Machine.interrupt().disable();
		/* Wake all threads that are waiting on the condition variable */
		KThread thread = null;
		do {
			thread = waitingThreadQueue.nextThread();
			if (thread != null) {
				thread.ready();
			}
		} while (thread != null);
		Machine.interrupt().restore(oldInterruptStatus);
		// edit ends
	}

	/** Lock associated with this condition variable */
	private Lock conditionLock;

	// FIXME Touhid : My edited block
	/** Threads waiting in the queue for this condition to be signaled */
	private ThreadQueue waitingThreadQueue = null;

	public static void selfTest() {
		Condition2Tester.runTest();
	}
}
