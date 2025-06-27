package com.poc.case_processing_service.config;

import com.arangodb.ArangoDB;
import com.arangodb.ArangoDatabase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ArangoDBConfig {

    @Value("${arangodb.host}")
    private String host;

    @Value("${arangodb.port}")
    private int port;

    @Value("${arangodb.database}")
    private String database;

    @Value("${arangodb.username}")
    private String username;

    @Value("${arangodb.password}")
    private String password;

    @Bean
    public ArangoDB arangoDB() {
        return new ArangoDB.Builder()
                .host(host, port)
                .user(username)
                .password(password)
                .build();
    }

    @Bean
    public ArangoDatabase arangoDatabase(ArangoDB arangoDB) {
        ArangoDatabase db = arangoDB.db(database);

        if (!db.exists()) {
            arangoDB.createDatabase(database);
            db = arangoDB.db(database);
        }

        createCollectionsIfNotExist(db);

        return db;
    }

    private void createCollectionsIfNotExist(ArangoDatabase db) {
        if (!db.collection("cases").exists()) {
            db.createCollection("cases");
        }
        if (!db.collection("persons").exists()) {
            db.createCollection("persons");
        }
        if (!db.collection("locations").exists()) {
            db.createCollection("locations");
        }
        if (!db.collection("evidence").exists()) {
            db.createCollection("evidence");
        }

        if (!db.collection("case_person").exists()) {
            db.createCollection("case_person");
        }
        if (!db.collection("case_location").exists()) {
            db.createCollection("case_location");
        }
        if (!db.collection("case_evidence").exists()) {
            db.createCollection("case_evidence");
        }
        if (!db.collection("person_location").exists()) {
            db.createCollection("person_location");
        }
    }
}
