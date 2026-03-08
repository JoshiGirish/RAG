#!/bin/bash

mvn install:install-file \
  -Dfile=../../4.Create_embeddings_generator_client/client/target/client-0.0.1.jar \
  -DgroupId=com.dev.ws.stapi.client \
  -DartifactId=embedding-generator \
  -Dversion=0.0.1 \
  -Dpackaging=jar