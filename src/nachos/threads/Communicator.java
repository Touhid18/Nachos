package nachos.threads;

import nachos.machine.Machine;

/**
 * A <i>communicator</i> allows threads to synchronously exchange 32-bit
 * messages. Multiple threads can be waiting to <i>speak</i>, and multiple
 * threads can be waiting to <i>listen</i>. But there should never be a time
 * when both a speaker and a listener are waiting, because the two threads can
 * be paired off at this point.
 */
public class Communicator {

	private ThreadQueue listenerThreadQueue;
	private ThreadQueue speakerThreadQueue;
	private int bufferedTransferrable;

	/**
	 * Allocate a new communicator.
	 */
	public Communicator() {
		this.listenerThreadQueue = ThreadedKernel.scheduler
				.newThreadQueue(true);
		this.speakerThreadQueue = ThreadedKernel.scheduler.newThreadQueue(true);
	}

	/**
	 * Wait for a thread to listen through this communicator, and then transfer
	 * <i>word</i> to the listener.
	 * 
	 * <p>
	 * Does not return until this thread is paired up with a listening thread.
	 * Exactly one listener should receive <i>word</i>.
	 * 
	 * @param wordToTransfer
	 *            the integer to transfer.
	 */
	public void speak(int wordToTransfer) {
		// Disable interrupts
		boolean oldInterruptStatus = Machine.interrupt().disable();

		KThread thread = null;

		// While there is no listener in the listener queue
		while ((thread = listenerThreadQueue.nextThread()) == null) {
			// Puts speaker in the speaker queue
			this.speakerThreadQueue.waitForAccess(KThread.currentThread());
			// Puts speaker to sleep
			KThread.sleep();
		}

		bufferedTransferrable = wordToTransfer;

		// Wakes up the listener
		thread.ready();

		// Restores interrupts
		Machine.interrupt().restore(oldInterruptStatus);
	}

	/**
	 * Wait for a thread to speak through this communicator, and then return the
	 * <i>word</i> that thread passed to <tt>speak()</tt>.
	 * 
	 * @return the integer transferred.
	 */
	public int listen() {
		// Disable interrupts
		boolean oldInterruptStatus = Machine.interrupt().disable();

		KThread thread = null;

		// Gets the first speaker in the speaker queue
		thread = speakerThreadQueue.nextThread();

		// Puts the listener in the listener queue
		this.listenerThreadQueue.waitForAccess(KThread.currentThread());

		// If there is a speaker
		if (thread != null) {
			// Wake up that speaker
			thread.ready();
		}

		// Puts the listener to sleep
		KThread.sleep();

		// Restores interrupts and returns the buffer (word)
		Machine.interrupt().restore(oldInterruptStatus);
		return bufferedTransferrable;
	}
	
	/**
	 * Tests whether this module is working.
	 */
	public static void selfTest() {
		CommunicatorTester.runTest();
	}
}
