package layer.service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import layer.model.user.RequestBody;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Checks {

	private final Set<String> SOCIAL_MEDIA_NAMES = Set.of("linkedin", "telegram", "skype", "instagram", "facebook");

	public Set<String> getIncorrectSocialMedia(Map<String, String> socialMedia) {
		Set<String> incorrectSocialMedia = new HashSet<>(socialMedia.keySet());
		incorrectSocialMedia.removeAll(SOCIAL_MEDIA_NAMES);
		return incorrectSocialMedia;
	}

	public boolean isValidAgeParameter(Optional<RequestBody> parameters) {
		return parameters.map(RequestBody::getAgeLimits).map(Checks::isValidLowerUpperAgeLimits).orElse(false);
	}

	public boolean isValidLowerUpperAgeLimits(List<String> ageLimits) {
		String lowerAgeLimit = ageLimits.get(0);
		String upperAgeLimit = ageLimits.get(1);
		if (lowerAgeLimit == null || upperAgeLimit == null) {
			return false;
		}
		Optional<Integer> lowerLimit = Utils.getIntegerValue(lowerAgeLimit);
		Optional<Integer> upperLimit = Utils.getIntegerValue(upperAgeLimit);
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

	public boolean isValidTableLastHashKey(String lastHashKey) {
		if (lastHashKey == null) {
			return false;
		}
		return !lastHashKey.isEmpty();
	}

	public boolean isValidIndexHashAndRangeKeys(String lastHashKey, String lastRangeKey) {
		if (lastHashKey == null || lastRangeKey == null) {
			return false;
		}
		return !lastHashKey.isBlank() && !lastRangeKey.isBlank();
	}

	public boolean isValidLimit(Optional<Integer> limit) {
		return limit.map(value -> value.intValue() > 0).orElse(false);
	}

}
