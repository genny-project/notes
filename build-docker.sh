#!/bin/bash
version=3.1.0
#./mvnw clean package -Pnative -Dnative-image.docker-build=true
pushd $PWD
./mvnw clean package -Dquarkus.container-image.build=true -DskipTests=true
docker tag ${USER}/notes:${version}  gennyproject/notes:latest 
docker tag ${USER}/notes:${version}  gennyproject/notes:${version} 
popd
