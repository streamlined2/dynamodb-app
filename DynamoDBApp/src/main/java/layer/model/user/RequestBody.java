package layer.model.user;

import layer.model.SortingOrder;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RequestBody {

	private static final int MIN_AGE = 0;
	private static final int MAX_AGE = 150;

	private String name;
	private String location;
	private Integer minAge;
	private Integer maxAge;
	private SortingOrder sorting;

	public boolean isAgeValid() {
		return minAge != null && maxAge != null && isAgeLimitValid(minAge.intValue())
				&& isAgeLimitValid(maxAge.intValue()) && minAge.intValue() <= maxAge.intValue();
	}

	private static boolean isAgeLimitValid(int limit) {
		return MIN_AGE <= limit && limit <= MAX_AGE;
	}

	public int getMinAgeOrDefault() {
		if (minAge == null) {
			return MIN_AGE;
		}
		return minAge.intValue();
	}

	public int getMaxAgeOrDefault() {
		if (maxAge == null) {
			return MAX_AGE;
		}
		return maxAge.intValue();
	}

	public boolean isLocationValid() {
		return location != null && !location.isBlank();
	}

	public boolean isNameValid() {
		return name != null && !name.isBlank();
	}

}
