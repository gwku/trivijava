./mvnw spring-boot:build-image -Dspring-boot.build-image.imageName=ghcr.io/gwku/trivijava
echo $GITHUB_TOKEN | docker login ghcr.io -u gwku --password-stdin
docker push ghcr.io/gwku/trivijava:latest
