package layer.model;

import java.util.List;
import java.util.Map;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;

public class NonFilteredResultList<E extends Entity<?>> extends AbstractResultList<E> {

	public NonFilteredResultList(DynamoDBMapper dbMapper, Class<E> entityClass, ListParameters listParameters,
			String partitionKey) {
		super(dbMapper, entityClass, listParameters, partitionKey);
	}

	@Override
	public List<E> fetchList() {
		if (!listParameters.hasValidLimit()) {
			return dbMapper.scan(entityClass, new DynamoDBScanExpression());
		}

		DynamoDBScanExpression scanExpression = new DynamoDBScanExpression().withConsistentRead(false)
				.withLimit(listParameters.getLimit());
		if (listParameters.hasValidTableLastHashKey()) {
			String lastHashKey = listParameters.getHashKey().get();
			scanExpression = scanExpression
					.withExclusiveStartKey(Map.of(tablePartitionKey, new AttributeValue().withS(lastHashKey)));
		}
		return dbMapper.scanPage(entityClass, scanExpression).getResults();
	}

}
