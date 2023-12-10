package layer.service.user;

import java.util.List;
import java.util.Optional;

import layer.model.ListParameters;
import layer.model.user.User;
import layer.model.user.UserData;

public interface UserService {

	List<User> getUserList(ListParameters listParameters);

	List<User> getUserListByQuery(ListParameters listParameters, UserData userData);

	void createUser(User user);

	void updateUser(String email, User user);

	void deleteUser(String email);

	Optional<User> findUser(String email);

}
