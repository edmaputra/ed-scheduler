#!/bin/bash

# Start PostgreSQL
echo "Starting PostgreSQL..."
docker-compose up -d

# Wait for PostgreSQL to be ready
echo "Waiting for PostgreSQL to be ready..."
sleep 5

# Run the application
echo "Starting Scheduler Application..."
./gradlew bootRun
