package nachos.threads;

/**
 * A Tester for the KThread class' join() implementation.
 * 
 * @author Touhid
 */
public class KThreadJoinTester {

	/**
	 * Class, which implements a KThread that simply prints out numbers in
	 * sequence.
	 */
	private static class LoopingThread implements Runnable {
		/* An ID for output purposes */
		private String looperName;
		/* The maximum number of iterations */
		private int upTo;

		public LoopingThread(String looperName, int upTo) {
			this.looperName = looperName;
			this.upTo = upTo;
		}

		@Override
		public void run() {
			for (int i = 0; i < upTo; i++) {
				System.out.println("--- " + looperName + " looped " + i
						+ " times");
				KThread.yield();
			}
			System.out.println("--- " + looperName + " done");
		}
	}

	/**
	 * JoinThread class, which implements a KThread that attempts to join with
	 * one or two threads, in sequence.
	 */
	private static class JoinThread implements Runnable {

		/* An ID for output purposes */
		private String joinedThreadName;
		/* The maximum number of iterations */
		private KThread firstKThread;
		private KThread secKThread;
		
		public JoinThread(String name, KThread fKThread, KThread sKThread) {
			this.joinedThreadName = name;
			this.firstKThread = fKThread;
			this.secKThread = sKThread;
		}

		@Override
		public void run() {
			/* Joining with the first thread, if non-null */
			if (firstKThread != null) {
				System.out.println("+++ " + joinedThreadName + " joining with "
						+ firstKThread.toString());
				firstKThread.join();
				System.out.println("+++ " + joinedThreadName + " joined with "
						+ firstKThread.toString());
			}
			/* Joining with the second thread, if non-null */
			if (secKThread != null) {
				System.out.println("+++ " + joinedThreadName + " joining with "
						+ secKThread.toString());
				secKThread.join();
				System.out.println("+++ " + joinedThreadName + " joined with "
						+ secKThread.toString());
			}
			System.out.println("+++ " + joinedThreadName + " done.");
		}
	}

	/**
	 * Main entry-point of this tester-class.
	 */
	public static void testKThreadJoin() {

		System.out.println("\n\t\t.... Starting to test KThread.join() ....\n");

		/*
		 * Create 5 LoopThread, each one looping 5*(i+1) times, so that the last
		 * create thread loops longer
		 */
		KThread loopingThreads[] = new KThread[5];
		for (int i = 0; i < 5; i++) {
			loopingThreads[i] = new KThread(new LoopingThread("loopingThread_"
					+ 5 * (i + 1), 5 * (i + 1)));
			loopingThreads[i].setName("loopingThread_" + 5 * (i + 1));
			loopingThreads[i].fork();
		}

		/*
		 * Create a JoinThread that waits for loopThread[1] and then for
		 * loopingThread[3]
		 */
		KThread joinThread1 = new KThread(new JoinThread("joinThread_1",
				loopingThreads[1], loopingThreads[3]));
		joinThread1.setName("joinThread_1");
		joinThread1.fork();

		/*
		 * Create a JoinThread that waits for loopThread[4] and then for
		 * loopingThread[2]
		 */
		KThread joinThread2 = new KThread(new JoinThread("joinThread_2",
				loopingThreads[4], loopingThreads[2]));
		joinThread2.setName("joinThread_2");
		joinThread2.fork();

		/*
		 * Create a JoinThread that waits for joinThread1 and then for
		 * loopingThread[4]
		 */
		KThread joinThread3 = new KThread(new JoinThread("joinThread_3",
				joinThread1, loopingThreads[4]));
		joinThread3.setName("joinThread_3");
		joinThread3.fork();

		/* Join with all the above to wait for the end of the testing */
		for (int i = 0; i < 5; i++) {
			loopingThreads[i].join();
		}
		joinThread1.join();
		joinThread2.join();
		joinThread3.join();

		System.out.println("\n\t\t.... KThread.join() testing ends ....\n");
	}

}
