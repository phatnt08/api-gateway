```bash
# Optionally, build and tag the image for pushing to a Docker registry
docker build -t phatnt8888/api-gateway:1.0.0 .

# Push the tagged image to the Docker registry
docker image push phatnt8888/api-gateway:1.0.0

# Pull iamge just push to docke hub
docker pull phatnt8888/api-gateway:1.0.0

# Run docker image phatnt8888/api-gateway:0.9.0 on docker desktop
docker run --network my-network --name api-gateway -p 8888:8888 -e IDENTITY_SERVICE_URI=http://identity-service:8081 -e PROFILE_SERVICE_URI=http://profile-service:8082 phatnt8888/api-gateway:1.0.0

```