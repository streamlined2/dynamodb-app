package layer.model;

import static java.util.Map.entry;
import static java.util.Map.ofEntries;

import java.text.MessageFormat;
import java.util.Map;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;

public class ValueFilteredResultList<E extends Entity<?>> extends FilteredResultList<E> {

	private final String sortKeyValue;

	public ValueFilteredResultList(DynamoDBMapper dbMapper, Class<E> entityClass, ListParameters listParameters,
			String tablePartitionKey, String indexPartitionKey, String indexPartitionKeyValue, String indexName,
			String sortKeyName, String sortKeyValue) {
		super(dbMapper, entityClass, listParameters, tablePartitionKey, indexPartitionKey, indexPartitionKeyValue,
				indexName, sortKeyName);
		this.sortKeyValue = sortKeyValue;
	}

	@Override
	protected AttributeValue getSortKeyAttributeValue(String rangeKey) {
		return new AttributeValue().withS(rangeKey);
	}

	@Override
	protected DynamoDBQueryExpression<E> getExpression() {
		final String partitionKeyLabel = "#" + indexPartitionKey;
		final String sortKeyLabel = "#" + sortKeyName;

		Map<String, String> expressionAttributeNames = ofEntries(entry(partitionKeyLabel, indexPartitionKey),
				entry(sortKeyLabel, sortKeyName));
		Map<String, AttributeValue> expressionAttributeValues = ofEntries(
				entry(":" + PARTITION_KEY_ALIAS, new AttributeValue().withS(indexPartitionKeyValue)),
				entry(":" + SORT_KEY_ALIAS, new AttributeValue().withS(sortKeyValue)));

		String conditionExpression = MessageFormat.format("{0} = :{1} and begins_with ({2}, :{3})", partitionKeyLabel,
				PARTITION_KEY_ALIAS, sortKeyLabel, SORT_KEY_ALIAS);
		return new DynamoDBQueryExpression<E>().withIndexName(indexName).withConsistentRead(false)
				.withKeyConditionExpression(conditionExpression).withExpressionAttributeNames(expressionAttributeNames)
				.withExpressionAttributeValues(expressionAttributeValues);
	}

}
