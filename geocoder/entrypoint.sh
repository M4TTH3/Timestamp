#!/bin/bash

directory="/app"

# Verify that the directory exist
if [ ! -d "${directory}/photon_data" ]; then
  echo "Directory /app/photon_data not found"
  exit 1
fi

echo "Starting geocoder"
cd ${directory}
java -jar photon-*.jar
