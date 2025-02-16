FROM openjdk:21-jdk-slim

WORKDIR /app

# Pull the latest jar file from github repo
ARG VERSION=0.6.1
ENV PHOTON_VERSION=${VERSION}

RUN apt-get update && apt-get install -y wget && apt-get clean
RUN wget https://github.com/komoot/photon/releases/download/${PHOTON_VERSION}/photon-${PHOTON_VERSION}.jar

COPY entrypoint.sh /app/entrypoint.sh

# Give execution permission
RUN chmod +x /app/entrypoint.sh

# Expose the port
EXPOSE 2322

# !IMPORTANT
# Must have photon-data directory in /app/photon_data
# This is for the ElasticSearch database. Share directory to main host
ENTRYPOINT ["/app/entrypoint.sh"]
