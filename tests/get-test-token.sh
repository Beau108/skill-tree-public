#!/usr/bin/env bash

EMAIL="beaualt01@gmail.com"
PASSWORD="0108beau"

response=$(curl -s -X POST "http://localhost:9099/identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=fake-api-key" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"${EMAIL}\",\"password\":\"${PASSWORD}\",\"returnSecureToken\":true}")

echo "$response" | jq -r .idToken

 