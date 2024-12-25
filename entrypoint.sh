#!/bin/bash

# Start the preprocessing step for the graph-cache
java -Xmx16g -jar /app/timestamp.jar --spring.profiles.active=graphhopper-import

# Start the Spring Boot application
java -jar /app/timestamp.jar
