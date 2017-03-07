package com.rs.mocking;

public final class MatchConverter {

	public enum ConversionType {
		TO_LIVEODDS, TO_LIVESCOUT, TO_LIVESCORE;
	}

	private ConversionType conversionType;

	public MatchConverter(final ConversionType conversionType) {
		this.conversionType = conversionType;
	}

	/**
	 * 
	 * @param match
	 * @return the number of attributes converted
	 */
	public int convert(final Match match) {
		int changedAttributes = 0;
		if (this.conversionType == ConversionType.TO_LIVEODDS) {
			System.out.println(String.format("Converted match %s to liveodds", match.getMatchId()));
			changedAttributes = 1;
		}
		else {
			throw new UnsupportedOperationException("Can only convert to Liveodds");
		}

		return changedAttributes;

	}
}
