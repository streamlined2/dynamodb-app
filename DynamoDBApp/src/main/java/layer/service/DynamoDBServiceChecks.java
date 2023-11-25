package layer.service;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import layer.model.RequestBody;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DynamoDBServiceChecks {

	private final Set<String> SOCIAL_MEDIA_NAMES = new TreeSet<>(
			Set.of("linkedin", "telegram", "skype", "instagram", "facebook"));

	boolean isValidSocialMedia(Map<String, String> socialMedia) {
		for (String key : socialMedia.keySet()) {
			if (!SOCIAL_MEDIA_NAMES.contains(key)) {
				return false;
			}
		}
		return true;
	}

	boolean isValidAgeParameter(RequestBody bodyParameters) {
		return bodyParameters.getAgeLimits() != null && isValidLowerUpperAgeLimits(bodyParameters);
	}

	boolean isValidLowerUpperAgeLimits(RequestBody bodyParameters) {
		String lowerAgeLimit = bodyParameters.getAgeLimits().get(0);
		String upperAgeLimit = bodyParameters.getAgeLimits().get(1);
		if (lowerAgeLimit == null || upperAgeLimit == null) {
			return false;
		}
		Integer lowerLimit = DynamoDBServiceUtils.getIntegerValue(lowerAgeLimit);
		Integer upperLimit = DynamoDBServiceUtils.getIntegerValue(upperAgeLimit);
		if (lowerLimit == null || upperLimit == null) {
			return false;
		}
		return lowerLimit >= 0 && lowerLimit < upperLimit;
	}

	boolean isValidLocationParameter(RequestBody bodyParameters) {
		return bodyParameters.getLocation() != null && !bodyParameters.getLocation().equals("");
	}

	boolean isValidNameParameter(RequestBody bodyParameters) {
		return bodyParameters.getName() != null && !bodyParameters.getName().equals("");
	}

	boolean isValidTableLastHashKey(String lastHashKey) {
		return lastHashKey != null && !lastHashKey.equals("");
	}

	boolean isValidIndexHashAndRangeKeys(String lastHashKey, String lastRangeKey) {
		return lastHashKey != null && lastRangeKey != null && !lastHashKey.equals("") && !lastRangeKey.equals("");
	}

	boolean isValidLimit(Integer limit) {
		return limit != null && limit > 0;
	}

}
