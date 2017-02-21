#DynamoDB example project

The present project is an example of Spring Boot one that makes use of Amazon DynamoDB as a persistence layer.

##Features

The following features are enabled in this implementation:

+ [Spring Data DynamoDB](https://github.com/derjust/spring-data-dynamodb)
+ DynamoDB Local (for unit tests)
+ Spring Data Rest
+ Table prefixing (for AWS single account support. For multi account setups, remove dynamoDBMapperConfigRef property in configuration classes).

##Implementation and Configuration

###Enabling Spring Data Rest
In order to enable Spring Data Rest support it was necessary to implement a DynamoDBMappingContext bean.
It is the key bean that lacks when SDR tries to get the initial set of entities (see DynamoDBAutoConfiguration.java)

### Configuration

You need to [setup](https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/setup-credentials.html) your AWS credentials to use it.
To configure the project, it is necessary to take into account these variables:

| Variable             | Description  | Default value |
|------------------|--------------|--------------|
| cloud.aws.region.static: | Your AWS region| eu-west-1|
| de.affinitas.dynamoDB.basePackage | The base package where Spring Data will search for your repositories | * |
| de.affinitas.environment | Your current environment (to be used in table prefixing) | development |


##How to build it

This project was created using Gradle (wrapper - which means you don´t need to install anything).

To build it, just run the following command:

``/gradlew clean build;``

