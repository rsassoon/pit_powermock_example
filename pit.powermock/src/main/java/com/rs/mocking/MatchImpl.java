package com.rs.mocking;

import java.time.LocalDateTime;

public class MatchImpl implements Match {

	private boolean changed;

	@Override
	public LocalDateTime getMatchDate() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isLiveOdds() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isLiveScout() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public MatchCoverage getMatchCoverage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public final long getMatchId() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setChanged(boolean changed) {
		this.changed = changed;
	}

	@Override
	public boolean isChanged() {
		return this.changed;
	}

}
