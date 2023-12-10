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

	@DynamoDBHashKey(attributeName = "email")
	@EqualsAndHashCode.Include
	private String email;

	@DynamoDBIndexHashKey(attributeName = "country", globalSecondaryIndexNames = { "country-name-index",
			"country-birthday-index", "country-location-index", "country-registration-index" })
	private String country;

	@DynamoDBIndexRangeKey(attributeName = "name", globalSecondaryIndexName = "country-name-index")
	private String name;

	@DynamoDBIndexRangeKey(attributeName = "location", globalSecondaryIndexName = "country-location-index")
	private String location;

	@DynamoDBIndexRangeKey(attributeName = "birthday", globalSecondaryIndexName = "country-birthday-index")
	private Long birthday;

	@DynamoDBIndexRangeKey(attributeName = "registration", globalSecondaryIndexName = "country-registration-index")
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