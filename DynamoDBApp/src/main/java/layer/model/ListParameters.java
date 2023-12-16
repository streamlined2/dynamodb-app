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

	public boolean hasValidTableLastHashKey() {
		return isValidKey(hashKey);
	}

	public boolean hasValidIndexHashAndRangeKeys() {
		return isValidKey(hashKey) && isValidKey(rangeKey);
	}

	private boolean isValidKey(Optional<String> key) {
		return key.map(k -> !k.isBlank()).orElse(false);
	}

	public boolean hasValidLimit() {
		return limit.flatMap(Utils::getIntegerValue).map(l -> l.intValue() > 0).orElse(false);
	}

	public Integer getLimit() {
		return limit.flatMap(Utils::getIntegerValue).orElse(MAX_LIMIT);
	}

}
