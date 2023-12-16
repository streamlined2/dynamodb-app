package layer.service.user;

import layer.model.AbstractResultList;
import layer.model.Entity;
import layer.model.ListParameters;
import layer.model.user.User;
import layer.model.user.UserData;
import layer.model.user.UserDto;
import layer.service.DynamoDBException;
import layer.service.GenericDynamoDBServiceImpl;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class DynamoDBUserServiceImpl extends GenericDynamoDBServiceImpl<User, UserDto> implements UserService {

	private static final Set<String> SOCIAL_MEDIA_NAMES = Set.of("linkedin", "telegram", "skype", "instagram",
			"facebook");
	private static final String USER_WITH_EMAIL_NOT_FOUND = "User with email %s not found";

	@Override
	public void createUser(UserDto userDto) {
		User existingUser = getDynamoDBMapper().load(User.class, userDto.getEmail());
		if (existingUser != null) {
			throw new DynamoDBException(String.format("User with email %s already exists", userDto.getEmail()));
		}
		checkSocialMedia(userDto, "User cannot be created: incorrect social media links %s");
		getDynamoDBMapper().save(userDto.toEntity());
	}

	private void checkSocialMedia(UserDto userDto, String message) {
		Set<String> incorrectSocialMedia = getIncorrectSocialMedia(userDto.getSocialMedia());
		if (!incorrectSocialMedia.isEmpty()) {
			throw new DynamoDBException(String.format(message, incorrectSocialMedia.toString()));
		}
	}

	private Set<String> getIncorrectSocialMedia(Set<String> socialMedia) {
		Set<String> incorrectSocialMedia = new HashSet<>(socialMedia);
		incorrectSocialMedia.removeAll(SOCIAL_MEDIA_NAMES);
		return incorrectSocialMedia;
	}

	@Override
	public Optional<UserDto> findUser(String email) {
		return Optional.ofNullable(getDynamoDBMapper().load(User.class, email)).map(User::toDto);
	}

	@Override
	public void updateUser(String email, UserDto userDto) {
		User existingUser = getDynamoDBMapper().load(User.class, email);
		if (existingUser == null) {
			throw new DynamoDBException(String.format(USER_WITH_EMAIL_NOT_FOUND, email));
		}
		checkSocialMedia(userDto, "User cannot be updated: incorrect social media links %s");
		User user = userDto.toEntity();
		user.setEmail(email);
		getDynamoDBMapper().save(user);
	}

	@Override
	public void deleteUser(String email) {
		User existingUser = getDynamoDBMapper().load(User.class, email);
		if (existingUser == null) {
			throw new DynamoDBException(String.format(USER_WITH_EMAIL_NOT_FOUND, email));
		}
		getDynamoDBMapper().delete(existingUser);
	}

	@Override
	public List<UserDto> getUserList(ListParameters listParameters) {
		return Entity
				.toDtoList(AbstractResultList.getNonFilteredUserList(getDynamoDBMapper(), listParameters).fetchList());
	}

	@Override
	public List<UserDto> getUserListByQuery(ListParameters listParameters, UserData userData) {
		return Entity
				.toDtoList(AbstractResultList.getUserList(getDynamoDBMapper(), listParameters, userData).fetchList());
	}

}
