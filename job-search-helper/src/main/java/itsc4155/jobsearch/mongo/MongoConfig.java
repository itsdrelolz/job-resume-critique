package itsc4155.jobsearch.mongo;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = {"itsc4155.jobsearch.user", "itsc4155.jobsearch.posting", "itsc4155.jobsearch.application", "itsc4155.jobsearch.notification"})
public class MongoConfig {

    // Injecting properties from application.properties
    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @Value("${spring.data.mongodb.database}")
    private String databaseName;

    @Bean
    public MongoClient mongoClient() {
        // Using the Mongo URI from the application.properties
        return MongoClients.create(mongoUri);
    }

    @Bean
    public MongoTemplate mongoTemplate() {
        // Return MongoTemplate connected to the correct database
        return new MongoTemplate(mongoClient(), databaseName);
    }

}
