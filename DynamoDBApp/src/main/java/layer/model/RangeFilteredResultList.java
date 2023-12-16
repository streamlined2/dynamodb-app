package layer.model;

import static java.util.Map.entry;
import static java.util.Map.ofEntries;

import java.text.MessageFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;

public class RangeFilteredResultList<E extends Entity<?>> extends FilteredResultList<E> {

	private static final String SORT_KEY_UP_ALIAS = "sortUpAlias";
	private static final String SORT_KEY_LOW_ALIAS = "sortLowAlias";

	private final Number sortKeyLowValue;
	private final Number sortKeyUpValue;

	public RangeFilteredResultList(DynamoDBMapper dbMapper, Class<E> entityClass, ListParameters listParameters,
			String tablePartitionKey, String indexPartitionKey, String indexPartitionKeyValue, String indexName,
			String sortKeyName, Number sortKeyLowValue, Number sortKeyUpValue) {
		super(dbMapper, entityClass, listParameters, tablePartitionKey, indexPartitionKey, indexPartitionKeyValue,
				indexName, sortKeyName);
		this.sortKeyLowValue = sortKeyLowValue;
		this.sortKeyUpValue = sortKeyUpValue;
	}

	@Override
	protected AttributeValue getSortKeyAttributeValue(String rangeKey) {
		return new AttributeValue().withN(rangeKey);
	}

	@Override
	protected DynamoDBQueryExpression<E> getExpression() {
		final String partitionKeyLabel = "#" + indexPartitionKey;
		final String sortKeyLabel = "#" + sortKeyName;

		String conditionExpression = MessageFormat.format("{0} = :{1} and {2} between :{3} and :{4}", partitionKeyLabel,
				PARTITION_KEY_ALIAS, sortKeyLabel, SORT_KEY_LOW_ALIAS, SORT_KEY_UP_ALIAS);

		Map<String, String> expressionAttributeNames = ofEntries(entry(partitionKeyLabel, indexPartitionKey),
				entry(sortKeyLabel, sortKeyName));
		Map<String, AttributeValue> expressionAttributeValues = ofEntries(
				entry(":" + PARTITION_KEY_ALIAS, new AttributeValue().withS(indexPartitionKeyValue)),
				entry(":" + SORT_KEY_LOW_ALIAS, new AttributeValue().withN(getSortKeyValue(sortKeyLowValue))),
				entry(":" + SORT_KEY_UP_ALIAS, new AttributeValue().withN(getSortKeyValue(sortKeyUpValue))));

		return new DynamoDBQueryExpression<E>().withIndexName(indexName).withConsistentRead(false)
				.withKeyConditionExpression(conditionExpression).withExpressionAttributeNames(expressionAttributeNames)
				.withExpressionAttributeValues(expressionAttributeValues);
	}

	private String getSortKeyValue(Number sortKeyValue) {
		return Instant.now().minus(sortKeyValue.longValue(), ChronoUnit.YEARS).toString();
	}

}
