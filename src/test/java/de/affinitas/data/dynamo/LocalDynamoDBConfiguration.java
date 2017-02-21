package de.affinitas.data.dynamo;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.socialsignin.spring.data.dynamodb.repository.config.EnableDynamoDBRepositories;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig.TableNameOverride;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMappingException;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.local.embedded.DynamoDBEmbedded;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ListTablesResult;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;

/**
 * Loads Local DynamoDB and create the necessary tables to run the unit tests.
 */
@EnableDynamoDBRepositories(dynamoDBMapperConfigRef = "dynamoDBMapperConfig", basePackages="${de.affinitas.dynamoDB.basePackage:*}")
public class LocalDynamoDBConfiguration {

    private static final Long READ_CAPACITY_UNITS = 5L;
    private static final Long WRITE_CAPACITY_UNITS = 5L;

    @Value("${de.affinitas.environment:development}")
    private String environment;

    @Value("${de.affinitas.dynamoDB.basePackage:*}")
    private String basePackage;


    /**
     * Enables Local DynamoDB.
     */
    @Bean
    public AmazonDynamoDB amazonDynamoDB() {
        AmazonDynamoDB amazonDynamoDB = DynamoDBEmbedded.create().amazonDynamoDB();

        createTables(amazonDynamoDB);

        return amazonDynamoDB;
    }

    @Bean
    public DynamoDBMapperConfig dynamoDBMapperConfig() {
        DynamoDBMapperConfig.Builder builder = new DynamoDBMapperConfig.Builder();
        builder.setTableNameOverride(TableNameOverride.withTableNamePrefix(environment + "-"));
        return builder.build();
    }

    /**
     * Creates the necessary tables.
     */
    private void createTables(AmazonDynamoDB amazonDynamoDB) {
        ListTablesResult listTablesResult = amazonDynamoDB.listTables();

        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);

        scanner.addIncludeFilter(new AnnotationTypeFilter(DynamoDBTable.class));

        for (BeanDefinition bd : scanner.findCandidateComponents(basePackage)) {
            Class<?> clazz = null;
            try {
                clazz = Class.forName(bd.getBeanClassName());
            } catch (ClassNotFoundException e) {
                // Never will happen. Do nothing
            }

            String tableName = String.format("%s-%s" , environment, clazz.getAnnotation(DynamoDBTable.class).tableName());

            if (!listTablesResult.getTableNames().contains(tableName)) {
                String keyName = null;
                for (Field field : clazz.getDeclaredFields()) {
                    if (field.isAnnotationPresent(DynamoDBHashKey.class)) {
                        keyName = field.getName();
                        break;
                    }
                }

                if (keyName == null) {
                    throw new DynamoDBMappingException(String.format("The class %s has a not valid Dynamo Hash Key",
                                                                     bd.getBeanClassName()));
                }

                List<AttributeDefinition> attributeDefinitions = new ArrayList<AttributeDefinition>();
                attributeDefinitions.add(new AttributeDefinition().withAttributeName(keyName).withAttributeType("S"));

                List<KeySchemaElement> keySchemaElements = new ArrayList<KeySchemaElement>();
                keySchemaElements.add(new KeySchemaElement().withAttributeName(keyName).withKeyType(KeyType.HASH));

                CreateTableRequest request =
                        new CreateTableRequest()
                                                .withTableName(tableName)
                                                .withKeySchema(keySchemaElements)
                                                .withAttributeDefinitions(attributeDefinitions)
                                                .withProvisionedThroughput(
                                                                           new ProvisionedThroughput().withReadCapacityUnits(READ_CAPACITY_UNITS)
                                                                                                      .withWriteCapacityUnits(WRITE_CAPACITY_UNITS));

                amazonDynamoDB.createTable(request);
            }

        }

    }

}
