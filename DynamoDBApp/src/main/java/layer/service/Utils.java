package layer.service;

import java.util.Calendar;
import java.util.Optional;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Utils {

	private final Integer MAX_LIMIT = Integer.MAX_VALUE;

	public Optional<Integer> getIntegerValue(String value) {
		try {
			return Optional.of(Integer.valueOf(value));
		} catch (NumberFormatException e) {
			return Optional.empty();
		}
	}

	public Integer getLimit(Optional<String> limit) {
		return limit.flatMap(Utils::getIntegerValue).orElse(MAX_LIMIT);
	}

	public String getSortKeyValue(Number sortKeyValue) {
		long currentTimeInSeconds = getCurrentTimeInSeconds();
		long yearDurationInSeconds = getYearDurationInSeconds();
		return String.valueOf(currentTimeInSeconds - sortKeyValue.longValue() * yearDurationInSeconds);
	}

	private long getCurrentTimeInSeconds() {
		return Calendar.getInstance().getTimeInMillis() / 1000;
	}

	private long getYearDurationInSeconds() {
		Calendar cal = Calendar.getInstance();
		long currentTimestamp = cal.getTimeInMillis() / 1000;
		cal.add(Calendar.YEAR, 1);
		long nextYearTimestamp = cal.getTimeInMillis() / 1000;
		return nextYearTimestamp - currentTimestamp;
	}

}
