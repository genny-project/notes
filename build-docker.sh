#!/bin/bash
#./mvnw clean package -Pnative -Dnative-image.docker-build=true
pushd $PWD
./mvnw clean package -Dquarkus.container-image.build=true -DskipTests=true
docker tag ${USER}/notes:3.1.0  gennyproject/notes:latest 
#docker build -f src/main/docker/Dockerfile.jvm  -t gennyproject/notes:latest .
popd
