package de.affinitas.data.dynamo;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.socialsignin.spring.data.dynamodb.mapping.DynamoDBMappingContext;
import org.socialsignin.spring.data.dynamodb.repository.config.EnableDynamoDBRepositories;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.aws.core.region.RegionProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig.TableNameOverride;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

/**
 * Auto configuration class to DynamoDB. Note that it implements BeanClassLoaderAware to enable the mapping context.
 */
@Configuration
@EnableDynamoDBRepositories(dynamoDBMapperConfigRef = "dynamoDBMapperConfig", basePackages = "${de.affinitas.dynamoDB.basePackage:*}")
public class DynamoDBAutoConfiguration implements BeanClassLoaderAware {

    @Autowired
    private Environment environment;

    @Autowired
    private ResourceLoader resourceLoader;

    private ClassLoader classLoader;

    @Bean
    public AmazonDynamoDB amazonDynamoDB(
            @Autowired AWSCredentialsProvider awsCredentialsProvider,
            @Autowired RegionProvider regionProvider) {
        AmazonDynamoDB amazonDynamoDB = AmazonDynamoDBClientBuilder
                                                                   .standard()
                                                                   .withCredentials(awsCredentialsProvider)
                                                                   .withRegion(regionProvider.getRegion().getName())
                                                                   .build();
        return amazonDynamoDB;
    }

    @Bean
    public DynamoDBMapperConfig dynamoDBMapperConfig(
            @Value("${de.affinitas.environment:development}") String environmentPrefix) {
        DynamoDBMapperConfig.Builder builder = new DynamoDBMapperConfig.Builder();
        builder.setTableNameOverride(TableNameOverride.withTableNamePrefix(environmentPrefix + "-"));
        return builder.build();
    }

    //Creates a Mapping context bean to enable Spring Data Rest. - Start
    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Bean
    @ConditionalOnMissingBean
    public DynamoDBMappingContext mappingContext(BeanFactory beanFactory)
            throws ClassNotFoundException {
        DynamoDBMappingContext context = new DynamoDBMappingContext();
        context.setInitialEntitySet(getInitialEntitySet(beanFactory));

        return context;
    }

    private Set<Class<?>> getInitialEntitySet(BeanFactory beanFactory)
            throws ClassNotFoundException {
        Set<Class<?>> entitySet = new HashSet<Class<?>>();
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(
                                                                                                              false);
        scanner.setEnvironment(this.environment);
        scanner.setResourceLoader(this.resourceLoader);
        scanner.addIncludeFilter(new AnnotationTypeFilter(DynamoDBTable.class));
        for (String basePackage : getMappingBasePackages(beanFactory)) {
            if (StringUtils.hasText(basePackage)) {
                for (BeanDefinition candidate : scanner
                                                       .findCandidateComponents(basePackage)) {
                    entitySet.add(ClassUtils.forName(candidate.getBeanClassName(),
                                                     this.classLoader));
                }
            }
        }
        return entitySet;
    }

    private static Collection<String> getMappingBasePackages(BeanFactory beanFactory) {
        try {
            return AutoConfigurationPackages.get(beanFactory);
        } catch (IllegalStateException ex) {
            // no auto-configuration package registered yet
            return Collections.emptyList();
        }
    }

    //Creates a Mapping context bean to enable Spring Data Rest. - End

}
