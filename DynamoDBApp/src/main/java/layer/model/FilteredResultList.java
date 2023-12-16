package layer.model;

import static java.util.Map.entry;
import static java.util.Map.ofEntries;

import java.util.List;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;

public abstract class FilteredResultList<E extends Entity<?>> extends AbstractResultList<E> {

	protected final String indexName;
	protected final String sortKeyName;
	protected final String indexPartitionKey;
	protected final String indexPartitionKeyValue;

	protected FilteredResultList(DynamoDBMapper dbMapper, Class<E> entityClass, ListParameters listParameters,
			String tablePartitionKey, String indexPartitionKey, String indexPartitionKeyValue, String indexName,
			String sortKeyName) {
		super(dbMapper, entityClass, listParameters, tablePartitionKey);
		this.indexName = indexName;
		this.sortKeyName = sortKeyName;
		this.indexPartitionKey = indexPartitionKey;
		this.indexPartitionKeyValue = indexPartitionKeyValue;
	}

	@Override
	public List<E> fetchList() {
		DynamoDBQueryExpression<E> queryExpression = getExpression();

		if (listParameters.hasValidLimit()) {
			queryExpression = queryExpression.withLimit(listParameters.getLimit());
		}
		if (listParameters.hasValidIndexHashAndRangeKeys()) {
			String hashKey = listParameters.getHashKey().get();
			String rangeKey = listParameters.getRangeKey().get();
			queryExpression = queryExpression
					.withExclusiveStartKey(ofEntries(entry(tablePartitionKey, new AttributeValue().withS(hashKey)),
							entry(indexPartitionKey, new AttributeValue().withS(indexPartitionKeyValue)),
							entry(sortKeyName, getSortKeyAttributeValue(rangeKey))));
		}
		return dbMapper.queryPage(entityClass, queryExpression).getResults();
	}
	
	protected abstract AttributeValue getSortKeyAttributeValue(String rangeKey);

	protected abstract DynamoDBQueryExpression<E> getExpression();

}
