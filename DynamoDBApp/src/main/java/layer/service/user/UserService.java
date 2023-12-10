package layer.service.user;

import java.util.List;
import java.util.Optional;

import layer.model.ListParameters;
import layer.model.user.UserData;
import layer.model.user.UserDto;

public interface UserService {

	List<UserDto> getUserList(ListParameters listParameters);

	List<UserDto> getUserListByQuery(ListParameters listParameters, UserData userData);

	void createUser(UserDto user);

	void updateUser(String email, UserDto user);

	void deleteUser(String email);

	Optional<UserDto> findUser(String email);

}
