package layer.model.user;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

import layer.model.Entity;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;
import java.util.Set;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
@DynamoDBTable(tableName = "it-marathon-v3-user-db")
public class User implements Entity<UserDto> {

	public static final String EMAIL_KEY = "email";
	public static final String TABLE_PARTITION_KEY = EMAIL_KEY;

	public static final String INDEX_PARTITION_KEY = "country";
	public static final String INDEX_PARTITION_KEY_VALUE = "Ukraine";

	public static final String NAME_BODY_PARAMETER = "name";
	public static final String LOCATION_BODY_PARAMETER = "location";
	public static final String BIRTHDAY_BODY_PARAMETER = "birthday";
	public static final String REGISTRATION_BODY_PARAMETER = "registration";

	public static final String COUNTRY_NAME_INDEX = "country-name-index";
	public static final String COUNTRY_LOCATION_INDEX = "country-location-index";
	public static final String COUNTRY_BIRTHDAY_INDEX = "country-birthday-index";
	public static final String COUNTRY_REGISTRATION_INDEX = "country-registration-index";

	@DynamoDBHashKey(attributeName = "email")
	@EqualsAndHashCode.Include
	private String email;

	@DynamoDBIndexHashKey(attributeName = "country", globalSecondaryIndexNames = { COUNTRY_NAME_INDEX,
			COUNTRY_BIRTHDAY_INDEX, COUNTRY_LOCATION_INDEX, COUNTRY_REGISTRATION_INDEX })
	private String country;

	@DynamoDBIndexRangeKey(attributeName = NAME_BODY_PARAMETER, globalSecondaryIndexName = COUNTRY_NAME_INDEX)
	private String name;

	@DynamoDBIndexRangeKey(attributeName = LOCATION_BODY_PARAMETER, globalSecondaryIndexName = COUNTRY_LOCATION_INDEX)
	private String location;

	@DynamoDBIndexRangeKey(attributeName = BIRTHDAY_BODY_PARAMETER, globalSecondaryIndexName = COUNTRY_BIRTHDAY_INDEX)
	private Long birthday;

	@DynamoDBIndexRangeKey(attributeName = REGISTRATION_BODY_PARAMETER, globalSecondaryIndexName = COUNTRY_REGISTRATION_INDEX)
	private Long registration;

	@DynamoDBAttribute(attributeName = "avatar")
	private String avatar;

	@DynamoDBAttribute(attributeName = "about")
	private String about;

	@DynamoDBAttribute(attributeName = "interests")
	private List<String> interests;

	@DynamoDBAttribute(attributeName = "social_media")
	private Set<String> socialMedia;

	public UserDto toDto() {
		return UserDto.builder().email(getEmail()).country(getCountry()).name(getName()).location(getLocation())
				.birthday(getBirthday()).registration(getRegistration()).avatar(getAvatar()).about(getAbout())
				.interests(List.copyOf(getInterests())).socialMedia(Set.copyOf(getSocialMedia())).build();
	}

}