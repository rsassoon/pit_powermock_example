package com.rs.mocking;

import java.time.LocalDateTime;

public interface Match {

	enum MatchCoverage {
		COVERED, NOT_COVERED, TEST;
	}

	LocalDateTime getMatchDate();

	boolean isLiveOdds();

	boolean isLiveScout();

	MatchCoverage getMatchCoverage();

	long getMatchId();

	void setChanged(boolean flag);

	boolean isChanged();

}
