package layer.service.user;

import java.util.List;
import java.util.Optional;

import layer.model.user.RequestBody;
import layer.model.user.User;

public interface UserService {

	List<User> getUserList(Optional<String> lastKey, Optional<String> limit);

	List<User> getUserListByQuery(Optional<String> rangeKey, Optional<String> lastKey, Optional<String> limit,
			Optional<RequestBody> parameters);

	void createUser(User user);

	void updateUser(String email, User user);

	void deleteUser(String email);

	Optional<User> findUser(String email);

}
