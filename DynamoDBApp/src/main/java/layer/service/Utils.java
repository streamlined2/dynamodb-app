package layer.service;

import java.util.Calendar;
import java.util.Optional;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Utils {

	private final Integer MAX_LIMIT = Integer.MAX_VALUE;
	private final int MIN_AGE = 0;
	private final int MAX_AGE = 150;

	public Optional<Integer> getIntegerValue(String value) {
		try {
			return Optional.of(Integer.valueOf(value));
		} catch (NullPointerException | NumberFormatException e) {
			return Optional.empty();
		}
	}

	public long getLongValueOrDefault(String value, long defaultValue) {
		try {
			return Long.valueOf(value);
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	public Integer getLimit(Optional<String> limit) {
		return limit.flatMap(Utils::getIntegerValue).orElse(MAX_LIMIT);
	}

	public long getYearDurationInSeconds() {
		Calendar cal = Calendar.getInstance();
		long currentTimestamp = cal.getTimeInMillis() / 1000;
		cal.add(Calendar.YEAR, 1);
		long nextYearTimestamp = cal.getTimeInMillis() / 1000;
		return nextYearTimestamp - currentTimestamp;
	}

	public long getCurrentTimeInSeconds() {
		return Calendar.getInstance().getTimeInMillis() / 1000;
	}

	public String getSortKeyUpperValue(String sortKeyLowValue) {
		long currentTimeInSeconds = getCurrentTimeInSeconds();
		long yearDurationInSeconds = getYearDurationInSeconds();
		return String.valueOf(
				currentTimeInSeconds - getLongValueOrDefault(sortKeyLowValue, MIN_AGE) * yearDurationInSeconds);
	}

	public String getSortKeyLowValue(String sortKeyUpValue) {
		long currentTimeInSeconds = getCurrentTimeInSeconds();
		long yearDurationInSeconds = getYearDurationInSeconds();
		return String
				.valueOf(currentTimeInSeconds - getLongValueOrDefault(sortKeyUpValue, MAX_AGE) * yearDurationInSeconds);
	}

}
