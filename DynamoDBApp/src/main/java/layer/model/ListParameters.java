package layer.model;

import java.util.Optional;

import lombok.Builder;
import lombok.Value;
import utils.Utils;

@Value
@Builder
public class ListParameters {

	private static final Integer MAX_LIMIT = Integer.MAX_VALUE;

	private Optional<String> limit;
	private Optional<String> hashKey;
	private Optional<String> rangeKey;

	public boolean isValidTableLastHashKey() {
		return hashKey.map(this::isNotBlank).orElse(false);
	}

	public boolean isValidIndexHashAndRangeKeys() {
		return isValidKey(hashKey) && isValidKey(rangeKey);
	}

	private boolean isValidKey(Optional<String> key) {
		return key.map(this::isNotBlank).orElse(false);
	}

	private boolean isNotBlank(String key) {
		return !key.isBlank();
	}

	public boolean hasValidLimit() {
		return limit.flatMap(Utils::getIntegerValue).map(this::isValidLimit).orElse(false);
	}

	private boolean isValidLimit(Integer limit) {
		return limit.intValue() > 0;
	}

	public Integer getLimit() {
		return limit.flatMap(Utils::getIntegerValue).orElse(MAX_LIMIT);
	}

}
