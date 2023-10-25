package com.ashok.kalaha.config;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

public interface MongoDBTestContainer {
  String MONGO_DB_DOCKER_IMAGE = "mongo:4.0.10";
  MongoDBContainer mongoDBContainer =
      new MongoDBContainer(DockerImageName.parse(MONGO_DB_DOCKER_IMAGE));

  @DynamicPropertySource
  static void registerProperties(DynamicPropertyRegistry registry) {
    if (!mongoDBContainer.isRunning()) mongoDBContainer.start();
    registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
  }
}
