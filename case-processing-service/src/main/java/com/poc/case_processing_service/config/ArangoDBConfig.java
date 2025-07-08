package com.poc.case_processing_service.config;

import com.arangodb.ArangoDB;
import com.arangodb.ArangoDatabase;
import com.arangodb.springframework.annotation.EnableArangoRepositories;
import com.arangodb.springframework.config.ArangoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@EnableArangoRepositories(basePackages = "com.poc.case_processing_service.repository")
public class ArangoDBConfig implements ArangoConfiguration {


    @Override
    public ArangoDB.Builder arango() {
        return new ArangoDB.Builder()
                .host("e217e1ee0ec0.arangodb.cloud", 8529)
                .user("root")
                .password("IuKB9T5QfAhhpYuvcLsN")
                .useSsl(true);
    }


    @Override
    public String database() {
        return "case_management";
    }


    @Bean
    public ArangoDatabase arangoDatabase() {
        return arango().build().db(database());
    }
}
