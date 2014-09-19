package nachos.threads;

import nachos.machine.*;

import java.util.Random;

/**
 * A Tester for the Communicator class
 */
public class CommunicatorTester {

	/* Number of RV actions per thread */
	private static final int NUM_RV_ACTIONS_PER_THREAD = 5;
	/* Bounds on delay between attempts to speak/listen */
	private static final int MIN_DELAY = 10000;
	private static final int MAX_DELAY = 1000000;
	/* Number of Threads. Must be EVEN!! */
	private static final int NUM_RV_THREADS = 8;

	/**
	 * CommunicatorTest class, which has threads do a bunch of "random"
	 * rendez-vous, producing output that indicates whether communicating
	 * threads do indeed rendez-vous or not.
	 */

	/**
	 * RVThread class, which implements a thread that does a bunch of
	 * rendez-vous.
	 */
	private static class RVThread implements Runnable {
		/* My name */
		private String name;
		/* The Communicator for speaking/listening */
		private Communicator comm;
		/* True if I am a speaker, false if I am a listener */
		private boolean isSpeaker;
		/* The number of iterations */
		private int howMany;
		/* The Communicator for signaling that I am done */
		private Communicator commFinished;
		/* Random number generator */
		private Random randomNum;

		/* Constructor */
		RVThread(String name, Communicator comm, boolean isSpeaker,
				int howMany, Communicator commFinished, Random rng) {
			this.name = name;
			this.comm = comm;
			this.isSpeaker = isSpeaker;
			this.howMany = howMany;
			this.commFinished = commFinished;
			this.randomNum = rng;
		}

		/**
		 * Method to generate a random number of ticks that needs to be spent
		 * waiting before attempting a speak/listen actions. The number of ticks
		 * is generated to be between minDelay and maxDelay, inclusive
		 */
		private int randomDelay() {
			return MIN_DELAY + randomNum.nextInt(1 + MAX_DELAY - MIN_DELAY) - 1;
		}

		/**
		 * Method to generate a random word that a speaker will speak through
		 * the Communicator.
		 */
		private int randomWord() {
			return randomNum.nextInt(50); /* between 0 and 50 */
		}

		/**
		 * run() method for the RVThread. This method loops howMany times.
		 * During this loop the thread waits some random amount of time, then
		 * speaks or listen, depending on whether this.isSpeaker is sets to true
		 * or false.
		 */
		public void run() {

			System.out.println("** " + name + " begins.");

			/* Main loop */
			for (int i = 0; i < howMany; i++) {
				/* Sleep for some random delay */
				int randomDelay = randomDelay();
				System.out.println("** " + name + ": Sleeping for "
						+ randomDelay + " (i.e., until time="
						+ (randomDelay + Machine.timer().getTime()) + ")");
				ThreadedKernel.alarm.waitUntil(randomDelay);
				System.out.println("** " + name + ": Done sleeping! (time="
						+ Machine.timer().getTime() + ")");

				if (isSpeaker) {
					/* I am a speaker and I speak my word */
					int randomWord = randomWord();
					System.out.println("** " + name + ": Speaking "
							+ randomWord + " (time="
							+ Machine.timer().getTime() + ")");
					comm.speak(randomWord);
					System.out.println("** " + name
							+ ": Spoke and Returned (time="
							+ Machine.timer().getTime() + ")");
				} else {
					/* I am a listener and I listen */
					System.out.println("** " + name + ": Listening (time="
							+ Machine.timer().getTime() + ")");
					int word = comm.listen();
					System.out.println("** " + name + ": Listened and got "
							+ word + " (time=" + Machine.timer().getTime()
							+ ")");
				}
			}

			/* Exits and signals it to the main thread */
			commFinished.speak(-1);
			System.out.println("** " + name + " exits.");
		}
	}

	/**
	 * Tests whether this module is working.
	 */
	public static void runTest() {
		System.out.println("\n\t\t.... Communicator testing begins ....\n");

		/* Create a random number generator */
		Random randomNumber = new Random();

		/* Create the communicator on which RVThreads communicate */
		Communicator communicator = new Communicator();

		/* Create the communicator for listening to terminations */
		Communicator commFinished = new Communicator();

		/* Create rendezvous threads and fork them */
		KThread rvs[] = new KThread[NUM_RV_THREADS];
		for (int i = 0; i < NUM_RV_THREADS; i++) {
			if (i % 2 == 0) {
				/* Creating a speaker */
				rvs[i] = new KThread(new RVThread("RV-Thread(speaker)__" + i,
						communicator, true, NUM_RV_ACTIONS_PER_THREAD,
						commFinished, randomNumber));
				rvs[i].setName("RV-Thread(speaker)__" + i);
			} else {
				/* Creating a listener */
				rvs[i] = new KThread(new RVThread("RV-Thread(listener)__" + i,
						communicator, false, NUM_RV_ACTIONS_PER_THREAD,
						commFinished, randomNumber));
				rvs[i].setName("RV-Thread(listener)__" + i);
			}
			/* fork() */
			rvs[i].fork();
		}

		/*
		 * Wait for all threads to signal that they're done, which is done via a
		 * Communicator for good measures
		 */
		for (int i = 0; i < NUM_RV_THREADS; i++) {
			commFinished.listen();
			System.out.println("Acknowledged one thread exit.");
		}

		System.out.println("\n\t\t.... Communicator testing ends ....\n");

	}

}
