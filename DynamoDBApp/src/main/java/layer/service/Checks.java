package layer.service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import layer.model.user.RequestBody;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Checks {

	private final Set<String> SOCIAL_MEDIA_NAMES = Set.of("linkedin", "telegram", "skype", "instagram", "facebook");

	public Set<String> getIncorrectSocialMedia(Set<String> socialMedia) {
		Set<String> incorrectSocialMedia = new HashSet<>(socialMedia);
		incorrectSocialMedia.removeAll(SOCIAL_MEDIA_NAMES);
		return incorrectSocialMedia;
	}

	public boolean isValidAgeParameter(Optional<RequestBody> parameters) {
		return parameters.map(RequestBody::getAgeLimits).map(Checks::isValidLowerUpperAgeLimits).orElse(false);
	}

	public boolean isValidLowerUpperAgeLimits(List<String> ageLimits) {
		Optional<Integer> lowerLimit = Utils.getIntegerValue(ageLimits.get(0));
		Optional<Integer> upperLimit = Utils.getIntegerValue(ageLimits.get(1));
		if (lowerLimit.isEmpty() || upperLimit.isEmpty()) {
			return false;
		}
		return lowerLimit.get().intValue() >= 0 && lowerLimit.get().intValue() < upperLimit.get().intValue();
	}

	public boolean isValidLocationParameter(Optional<RequestBody> parameters) {
		return parameters.map(RequestBody::getLocation).map(location -> !location.isBlank()).orElse(false);
	}

	public boolean isValidNameParameter(Optional<RequestBody> parameters) {
		return parameters.map(RequestBody::getName).map(name -> !name.isBlank()).orElse(false);
	}

	public boolean isValidTableLastHashKey(Optional<String> lastHashKey) {
		return lastHashKey.map(hashKey -> !hashKey.isBlank()).orElse(false);
	}

	public boolean isValidIndexHashAndRangeKeys(Optional<String> lastHashKey, Optional<String> lastRangeKey) {
		return isValidKey(lastHashKey) && isValidKey(lastRangeKey);
	}

	public boolean isValidKey(Optional<String> lastHashKey) {
		return lastHashKey.map(key -> !key.isBlank()).orElse(false);
	}

	public boolean hasValidLimit(Optional<String> limit) {
		return limit.flatMap(Utils::getIntegerValue).map(Checks::isValidLimit).orElse(false);
	}

	public boolean isValidLimit(Integer limit) {
		return limit.intValue() > 0;
	}

}
