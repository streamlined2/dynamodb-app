package layer.model;

import java.util.List;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;

import layer.model.user.User;
import layer.model.user.UserData;

public abstract class AbstractResultList<E extends Entity<?>> {

	protected static final String PARTITION_KEY_ALIAS = "partAlias";
	protected static final String SORT_KEY_ALIAS = "sortAlias";

	protected final DynamoDBMapper dbMapper;
	protected final Class<E> entityClass;
	protected final ListParameters listParameters;
	protected final String tablePartitionKey;

	protected AbstractResultList(DynamoDBMapper dbMapper, Class<E> entityClass, ListParameters listParameters,
			String tablePartitionKey) {
		this.dbMapper = dbMapper;
		this.entityClass = entityClass;
		this.listParameters = listParameters;
		this.tablePartitionKey = tablePartitionKey;
	}

	public abstract List<E> fetchList();

	public static AbstractResultList<User> getUserList(DynamoDBMapper dbMapper, ListParameters listParameters,
			UserData userData) {
		if (userData.isNameValid()) {
			return new ValueFilteredResultList<>(dbMapper, User.class, listParameters, User.TABLE_PARTITION_KEY,
					User.INDEX_PARTITION_KEY, User.INDEX_PARTITION_KEY_VALUE, User.COUNTRY_NAME_INDEX,
					User.NAME_BODY_PARAMETER, userData.getName());
		}
		if (userData.isLocationValid()) {
			return new ValueFilteredResultList<>(dbMapper, User.class, listParameters, User.TABLE_PARTITION_KEY,
					User.INDEX_PARTITION_KEY, User.INDEX_PARTITION_KEY_VALUE, User.COUNTRY_LOCATION_INDEX,
					User.LOCATION_BODY_PARAMETER, userData.getLocation());
		}
		if (userData.isAgeValid()) {
			return new RangeFilteredResultList<>(dbMapper, User.class, listParameters, User.TABLE_PARTITION_KEY,
					User.INDEX_PARTITION_KEY, User.INDEX_PARTITION_KEY_VALUE, User.COUNTRY_BIRTHDAY_INDEX,
					User.BIRTHDAY_BODY_PARAMETER, userData.getMinAgeOrDefault(), userData.getMaxAgeOrDefault());
		}
		return getNonFilteredUserList(dbMapper, listParameters);
	}

	public static AbstractResultList<User> getNonFilteredUserList(DynamoDBMapper dbMapper,
			ListParameters listParameters) {
		return getNonFilteredResultList(dbMapper, User.class, listParameters, User.TABLE_PARTITION_KEY);
	}

	public static <E extends Entity<?>> AbstractResultList<E> getNonFilteredResultList(DynamoDBMapper dbMapper,
			Class<E> entityClass, ListParameters listParameters, String tablePartitionKey) {
		return new NonFilteredResultList<>(dbMapper, entityClass, listParameters, tablePartitionKey);
	}

}
