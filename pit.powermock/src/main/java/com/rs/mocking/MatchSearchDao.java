package com.rs.mocking;

public class MatchSearchDao {

	private static Thread printThread;

	static {
		printThread = new Thread(() -> System.out.println("Started"));
		printThread.setName("printThread");
		printThread.start();
	}

	public static Match findMatchById(final long matchid) {
		throw new UnsupportedOperationException("method not implemented");
	}

}
