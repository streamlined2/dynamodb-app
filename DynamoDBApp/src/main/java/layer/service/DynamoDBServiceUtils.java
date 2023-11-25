package layer.service;

import java.util.Calendar;
import java.util.Optional;

import lombok.experimental.UtilityClass;

@UtilityClass
public class DynamoDBServiceUtils {

	private final int MIN_AGE = 0;
	private final int MAX_AGE = 150;

	Integer getIntegerValue(String value) {
		try {
			return Integer.valueOf(value);
		} catch (NumberFormatException e) {
			return null;// TODO
		}
	}

	Optional<Integer> getOptionalIntegerValue(String value) {
		try {
			return Optional.of(Integer.valueOf(value));
		} catch (NumberFormatException e) {
			return Optional.empty();// TODO
		}
	}

	long getYearDurationInSeconds() {
		Calendar cal = Calendar.getInstance();
		long currentTimestamp = cal.getTimeInMillis() / 1000;
		cal.add(Calendar.YEAR, 1);
		long nextYearTimestamp = cal.getTimeInMillis() / 1000;
		return nextYearTimestamp - currentTimestamp;
	}

	long getCurrentTimeInSeconds() {
		return Calendar.getInstance().getTimeInMillis() / 1000;
	}

	String getSortKeyUpperValue(String sortKeyLowValue) {
		long currentTime = getCurrentTimeInSeconds();
		long yearValue = getYearDurationInSeconds();
		return String.valueOf(currentTime - getOptionalIntegerValue(sortKeyLowValue).orElse(MIN_AGE) * yearValue);
	}

	String getSortKeyLowValue(String sortKeyUpValue) {
		long currentTime = getCurrentTimeInSeconds();
		long yearValue = getYearDurationInSeconds();
		return String.valueOf(currentTime - getOptionalIntegerValue(sortKeyUpValue).orElse(MAX_AGE) * yearValue);
	}

}
