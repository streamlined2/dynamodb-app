package layer.service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import layer.model.RequestBody;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Checks {

	private final Set<String> SOCIAL_MEDIA_NAMES = Set.of("linkedin", "telegram", "skype", "instagram", "facebook");

	Set<String> getIncorrectSocialMedia(Map<String, String> socialMedia) {
		Set<String> incorrectSocialMedia = new HashSet<>(socialMedia.keySet());
		incorrectSocialMedia.removeAll(SOCIAL_MEDIA_NAMES);
		return incorrectSocialMedia;
	}

	boolean isValidAgeParameter(RequestBody bodyParameters) {
		List<String> ageLimits = bodyParameters.getAgeLimits();
		if (ageLimits == null) {
			return false;
		}
		return isValidLowerUpperAgeLimits(ageLimits);
	}

	boolean isValidLowerUpperAgeLimits(List<String> ageLimits) {
		String lowerAgeLimit = ageLimits.get(0);
		String upperAgeLimit = ageLimits.get(1);
		if (lowerAgeLimit == null || upperAgeLimit == null) {
			return false;
		}
		Integer lowerLimit = Utils.getIntegerValue(lowerAgeLimit);
		Integer upperLimit = Utils.getIntegerValue(upperAgeLimit);
		if (lowerLimit == null || upperLimit == null) {
			return false;
		}
		return lowerLimit.intValue() >= 0 && lowerLimit.intValue() < upperLimit.intValue();
	}

	boolean isValidLocationParameter(RequestBody bodyParameters) {
		if (bodyParameters.getLocation() == null) {
			return false;
		}
		return !bodyParameters.getLocation().isEmpty();
	}

	boolean isValidNameParameter(RequestBody bodyParameters) {
		if (bodyParameters.getName() == null) {
			return false;
		}
		return !bodyParameters.getName().isEmpty();
	}

	boolean isValidTableLastHashKey(String lastHashKey) {
		if (lastHashKey == null) {
			return false;
		}
		return !lastHashKey.isEmpty();
	}

	boolean isValidIndexHashAndRangeKeys(String lastHashKey, String lastRangeKey) {
		if (lastHashKey == null || lastRangeKey == null) {
			return false;
		}
		return !lastHashKey.isEmpty() && !lastRangeKey.isEmpty();
	}

	boolean isValidLimit(Integer limit) {
		if (limit == null) {
			return false;
		}
		return limit.intValue() > 0;
	}

}
