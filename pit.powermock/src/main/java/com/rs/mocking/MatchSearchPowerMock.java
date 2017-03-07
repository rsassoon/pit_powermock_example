package com.rs.mocking;

import com.rs.mocking.Match.MatchCoverage;
import com.rs.mocking.MatchConverter.ConversionType;

public class MatchSearchPowerMock {

	private final MatchConverter internalConverter;

	public MatchSearchPowerMock(final MatchConverter converter) {
		this.internalConverter = converter;
	}

	public boolean isMatchCovered(final long matchId) {
		return MatchSearchDao.findMatchById(matchId).getMatchCoverage() == MatchCoverage.COVERED;
	}

	/**
	 * 
	 * @param matchId
	 * @param conversionType
	 * @return 
	 * <li>true if at least one attribute for the match has changed</li>
	 * <li>false if no attributes for the match have changed or the match cannot be converted</li>
	 */
	public boolean searchAndConvertMatch(final long matchId, final ConversionType conversionType) {
		boolean matchChanged = false;
		if (canConvertMatch(matchId)) {
			final MatchConverter converter = new MatchConverter(conversionType);
			final Match match = MatchSearchDao.findMatchById(matchId);

			matchChanged = converter.convert(match) > 0;
			match.setChanged(matchChanged);
		}
		return matchChanged;
	}

	public boolean convertMatch(final Match match) {
		boolean matchChanged = false;
		if (canConvertMatch(match.getMatchId())) {
			matchChanged = internalConverter.convert(match) > 0;
			//match.setChanged(matchChanged);
		}
		return matchChanged;
	}

	private boolean canConvertMatch(final long matchId) {
		return matchId > 0;
	}

}
