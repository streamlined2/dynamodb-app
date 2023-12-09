package layer.service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Checks {

	private final Set<String> SOCIAL_MEDIA_NAMES = Set.of("linkedin", "telegram", "skype", "instagram", "facebook");

	public Set<String> getIncorrectSocialMedia(Set<String> socialMedia) {
		Set<String> incorrectSocialMedia = new HashSet<>(socialMedia);
		incorrectSocialMedia.removeAll(SOCIAL_MEDIA_NAMES);
		return incorrectSocialMedia;
	}

	public boolean isValidTableLastHashKey(Optional<String> lastHashKey) {
		return lastHashKey.map(Checks::isNotBlank).orElse(false);
	}

	public boolean isValidIndexHashAndRangeKeys(Optional<String> lastHashKey, Optional<String> lastRangeKey) {
		return isValidKey(lastHashKey) && isValidKey(lastRangeKey);
	}

	private boolean isValidKey(Optional<String> key) {
		return key.map(Checks::isNotBlank).orElse(false);
	}

	private boolean isNotBlank(String key) {
		return !key.isBlank();
	}

	public boolean hasValidLimit(Optional<String> limit) {
		return limit.flatMap(Utils::getIntegerValue).map(Checks::isValidLimit).orElse(false);
	}

	private boolean isValidLimit(Integer limit) {
		return limit.intValue() > 0;
	}

}
