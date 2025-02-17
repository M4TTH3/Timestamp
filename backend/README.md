## Environment Variables
The following environment variables are required to run the application:

    - DB_URL=jdbc:postgresql://{url}/{db}
    - DB_USER={user}
    - DB_PASSWORD={password}
    - DB_PASSWORD
    - ISSUER_URI=https://securetoken.google.com/{appname}
    - JWK_URI=https://www.googleapis.com/service_accounts/v1/jwk/securetoken%40system.gserviceaccount.com
    - OSM_FILE={path to osm file}
    - GEOCODER_URL=http://geocoder:2322

## Volumes
    - {hostpath}:/app/graph-cache/
    - {path}:/app/osm/

## Firebase Admin Key
Make sure to add backend/src/main/resources/firebase-admin-key.json when building.
