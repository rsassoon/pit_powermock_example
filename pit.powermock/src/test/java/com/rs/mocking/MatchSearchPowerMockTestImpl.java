package com.rs.mocking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.internal.WhiteboxImpl;

import com.rs.mocking.Match;
import com.rs.mocking.Match.MatchCoverage;
import com.rs.mocking.MatchConverter;
import com.rs.mocking.MatchConverter.ConversionType;
import com.rs.mocking.MatchImpl;
import com.rs.mocking.MatchSearchDao;
import com.rs.mocking.MatchSearchPowerMock;

@PrepareForTest({ MatchSearchDao.class /*mocking static methods*/, MatchSearchPowerMock.class /*to be able to use whenNew, and test private methods*/
		, MatchConverter.class /*to be able to mock a final class*/, MatchImpl.class /*to be able to mock final methods*/ })
@SuppressStaticInitializationFor({ "com.rs.mocking.MatchSearchDao" })
@RunWith(PowerMockRunner.class)
public class MatchSearchPowerMockTestImpl {
	/*
	 * to make ecl emma work with power mock
	 * http://www.notonlyanecmplace.com/make-eclemma-test-coverage-work-with-powermock/
	 * run configuration vm args => -javaagent:libs/powermock-module-javaagent-1.6.6.jar -noverify
	 * */
	//	@Rule
	//	public PowerMockRule rule = new PowerMockRule();

	@Mock
	private Match match;

	@Mock
	private MatchConverter converter;

	@Mock
	private MatchConverter internalConverter;

	private MatchSearchPowerMock matchSearch;

	@Spy
	private MatchImpl matchImplSpy = new MatchImpl();

	@Mock
	private MatchImpl matchImplMock;

	@Mock
	private PrintStream out;

	private long MATCH_ID = 1L;

	//	static {
	//		PowerMockAgent.initializeIfNeeded();
	//	}

	@Before
	public void setUp() throws Exception {
		matchSearch = new MatchSearchPowerMock(internalConverter);

		mockStatic(MatchSearchDao.class);

		//matchImplSpy = spy(new MatchImpl());
		//matchImplMock = PowerMockito.mock(MatchImpl.class);

		System.setOut(out);

		when(match.getMatchId()).thenReturn(MATCH_ID);

		when(MatchSearchDao.findMatchById(eq(MATCH_ID))).thenReturn(match);
		//when(MatchSearchDao.findMatchById(anyLong())).thenReturn(match);
	}

	@Test
	public void testIsMatchCovered_MatchIsNotCovered_ShouldReturnFalse() {
		when(match.getMatchCoverage()).thenReturn(MatchCoverage.NOT_COVERED);

		assertThat(matchSearch.isMatchCovered(MATCH_ID)).isFalse();
	}

	@Test
	public void testIsMatchCovered_MatchIsTest_ShouldReturnFalse() {
		when(match.getMatchCoverage()).thenReturn(MatchCoverage.TEST);

		assertThat(matchSearch.isMatchCovered(MATCH_ID)).isFalse();
	}

	@Test
	public void testIsMatchCovered_MatchIsCovered_ShouldReturnTrue() {
		when(match.getMatchCoverage()).thenReturn(MatchCoverage.COVERED);

		assertThat(matchSearch.isMatchCovered(MATCH_ID)).isTrue();
	}

	@Test
	public void testSearchAndConvertMatchToLiveOdds_MatchIdLessEqual0_ShouldReturnFalse() {
		assertThat(matchSearch.searchAndConvertMatch(0L, ConversionType.TO_LIVEODDS)).isFalse();
		assertThat(matchSearch.searchAndConvertMatch(-1L, ConversionType.TO_LIVESCOUT)).isFalse();
		verify(match, never()).setChanged(anyBoolean());
	}

	//@Ignore
	@Test
	public void testSearchAndConvertMatchToLiveOdds_RelyingOnInternalCollaborator_ShouldReturnTrue() {
		assertThat(matchSearch.searchAndConvertMatch(MATCH_ID, ConversionType.TO_LIVEODDS)).isTrue();
		verify(out).println(anyString());
		verify(match, times(1)).setChanged(eq(true));
	}

	//@Ignore
	@Test
	public void testSearchAndConvertMatchToLiveScout_RelyingOnInternalCollaborator_ShouldThrowUnsupportedOperationException() throws Exception {
		assertThatThrownBy(() -> matchSearch.searchAndConvertMatch(MATCH_ID, ConversionType.TO_LIVESCOUT)).isInstanceOf(UnsupportedOperationException.class).hasMessage("Can only convert to Liveodds");
	}

	@Test
	public void testSearchAndConvertMatchToLiveScout_RelyingOnWhenNewOperator_ChangedAttributes1_ShouldReturnTrue() throws Exception {
		final ConversionType conversion = ConversionType.TO_LIVESCOUT;
		PowerMockito.whenNew(MatchConverter.class).withArguments(conversion).thenReturn(converter);
		when(converter.convert(eq(match))).thenReturn(1);
		assertThat(matchSearch.searchAndConvertMatch(MATCH_ID, ConversionType.TO_LIVESCOUT)).isTrue();
		verify(match, times(1)).setChanged(eq(true));
	}

	@Test
	public void testPrivateCanConvertMatch_MatchIdLessEqual0_ShouldReturnFalse() throws Exception {
		assertThat((Boolean)WhiteboxImpl.invokeMethod(matchSearch, "canConvertMatch", -1L)).isFalse();
		assertThat((Boolean)WhiteboxImpl.invokeMethod(matchSearch, "canConvertMatch", 0L)).isFalse();
	}

	@Test
	public void testPrivateCanConvertMatch_MatchIdGreaterThan0_ShouldReturnFalse() throws Exception {
		assertThat((Boolean)WhiteboxImpl.invokeMethod(matchSearch, "canConvertMatch", 1L)).isTrue();
	}

	//@Ignore
	@Test
	public void testSearchAndConvertMatchToLiveOddsAndLiveScout_VerifySetChangedAndMatchIdAddedToLocalList() throws Exception {
		final List<Long> convertedMatches = new ArrayList<>();
		doAnswer(invocation -> convertedMatches.add(match.getMatchId())).when(match).setChanged(eq(true));

		assertThat(matchSearch.searchAndConvertMatch(MATCH_ID, ConversionType.TO_LIVEODDS)).isTrue();
		verify(match, times(1)).setChanged(eq(true));
		assertThat(convertedMatches).hasSize(1);
		verify(out).println(anyString());

		final ConversionType conversion = ConversionType.TO_LIVESCOUT;
		PowerMockito.whenNew(MatchConverter.class).withArguments(conversion).thenReturn(converter);
		when(converter.convert(eq(match))).thenReturn(0);

		assertThat(matchSearch.searchAndConvertMatch(MATCH_ID, conversion)).isFalse();
		verify(match, times(1)).setChanged(eq(false));
		assertThat(convertedMatches).hasSize(1);

		when(converter.convert(eq(match))).thenReturn(2);

		assertThat(matchSearch.searchAndConvertMatch(MATCH_ID, conversion)).isTrue();
		verify(match, times(2)).setChanged(eq(true));
		assertThat(convertedMatches).hasSize(2);
	}

	@Test
	public void testSearchAndConvertMatchV2_MatchIdLessEqual0_ShouldReturnFalse() {
		when(match.getMatchId()).thenReturn(0L);
		assertThat(matchSearch.convertMatch(match)).isFalse();
		when(match.getMatchId()).thenReturn(-1L);
		assertThat(matchSearch.convertMatch(match)).isFalse();
		//verify(match, never()).setChanged(anyBoolean());
	}

	@Test
	public void testSearchAndConvertMatchV2_ChangedAttributes1_ShouldReturnTrue() {
		when(internalConverter.convert(eq(match))).thenReturn(1);
		assertThat(matchSearch.convertMatch(match)).isTrue();
		//verify(match, times(1)).setChanged(eq(true));
	}

	@Test
	public void testSearchAndConvertMatchV2ToLiveOddsAndLiveScout_VerifySetChangedAndMatchIdAddedToLocalList() throws Exception {
		final List<Long> convertedMatches = new ArrayList<>();
		//doAnswer(invocation -> convertedMatches.add(match.getMatchId())).when(match).setChanged(eq(true));

		when(internalConverter.convert(eq(match))).thenReturn(1);
		assertThat(matchSearch.convertMatch(match)).isTrue();
		//verify(match, times(1)).setChanged(eq(true));
		//assertThat(convertedMatches).hasSize(1);

		when(internalConverter.convert(eq(match))).thenReturn(0);

		assertThat(matchSearch.convertMatch(match)).isFalse();
		//verify(match, times(1)).setChanged(eq(false));
		//assertThat(convertedMatches).hasSize(1);

		when(internalConverter.convert(eq(match))).thenReturn(2);

		assertThat(matchSearch.convertMatch(match)).isTrue();
		//verify(match, times(2)).setChanged(eq(true));
		//assertThat(convertedMatches).hasSize(2);
	}

	@Test
	public void testMatchImplSpy_WithoutStubbing_ShouldCallRealMethods() {
		assertThat(matchImplSpy.getMatchDate()).isNull();
		assertThat(matchImplSpy.isLiveOdds()).isFalse();
		assertThat(matchImplSpy.isLiveScout()).isFalse();
		assertThat(matchImplSpy.getMatchCoverage()).isNull();
		matchImplSpy.setChanged(true);
		assertThat(matchImplSpy.isChanged()).isTrue();
	}

	@Test
	public void testGetMatchIdOnMatchImplSpy_WithStubbing_ShouldReturnStubbedValue() {
		doReturn(MATCH_ID).when(matchImplSpy).getMatchId();
		assertThat(matchImplSpy.getMatchId()).isEqualTo(MATCH_ID);
	}

	@Test
	public void testSetChangedOnMatchImplSpy_DoNothing_ShouldReturnFalse() {
		doNothing().when(matchImplSpy).setChanged(anyBoolean());
		matchImplSpy.setChanged(true);
		assertThat(matchImplSpy.isChanged()).isFalse();
	}

	@Test
	public void testGetMatchIdOnMatchImplMock_WithStubbing_ShouldReturnStubbedValue() {
		when(matchImplMock.getMatchId()).thenReturn(MATCH_ID);
		assertThat(matchImplMock.getMatchId()).isEqualTo(MATCH_ID);
	}

	@Test
	public void testGetMatchIdOnMatchImplMock_CallRealMthod_ShouldReturn0() {
		when(matchImplMock.getMatchId()).thenCallRealMethod();
		assertThat(matchImplMock.getMatchId()).isEqualTo(0);
	}

}
