package layer.model.user;

import java.util.List;
import java.util.Set;

import layer.model.Dto;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UserDto implements Dto<User> {

	private String email;
	private String country;
	private String name;
	private String location;
	private Long birthday;
	private Long registration;
	private String avatar;
	private String about;
	private List<String> interests;
	private Set<String> socialMedia;

	public User toEntity() {
		return User.builder().email(getEmail()).country(getCountry()).name(getName()).location(getLocation())
				.birthday(getBirthday()).registration(getRegistration()).avatar(getAvatar()).about(getAbout())
				.interests(List.copyOf(getInterests())).socialMedia(Set.copyOf(getSocialMedia())).build();
	}

}
