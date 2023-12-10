package utils;

import java.util.Optional;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Utils {

	public Optional<Integer> getIntegerValue(String value) {
		try {
			return Optional.of(Integer.valueOf(value));
		} catch (NumberFormatException e) {
			return Optional.empty();
		}
	}

}
