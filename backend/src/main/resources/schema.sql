-- Table for users
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    pfp VARCHAR(255) NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Table for events
CREATE TABLE IF NOT EXISTS events (
    id SERIAL PRIMARY KEY,  -- Auto-incremented ID for the event
    creator VARCHAR(255) NOT NULL,  -- FK to users table
    name VARCHAR(255) NOT NULL,
    description TEXT,  -- Description of the event
    address VARCHAR(255) NOT NULL,  -- Event location address
    latitude DOUBLE PRECISION NOT NULL,  -- Event location latitude
    longitude DOUBLE PRECISION NOT NULL, -- Event location longitude
    arrival TIMESTAMP NOT NULL,  -- Time when the event starts
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,  -- Auto-timestamp when created
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP  -- Auto-timestamp when updated
);

-- Table for the many-to-many relationship between users and events
CREATE TABLE IF NOT EXISTS user_events (
     user_id VARCHAR(255),  -- FK to users table
     event_id BIGINT,  -- FK to events table
     PRIMARY KEY (user_id, event_id),
     CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
     CONSTRAINT fk_event FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE
);