#!/bin/bash

cd ..
source .env
echo "USER = $USER"
echo "REMOTE_HOST = $REMOTE_HOST"
echo "REMOTE_FPATH = $REMOTE_FPATH"
echo "APP_TYPE = $APP_TYPE"

set -ex

# =========== CLEAN ===========

# local
./gradlew clean --refresh-dependencies --no-build-cache

# remote
ssh "$USER@$REMOTE_HOST" "cd '$REMOTE_FPATH/comp-512/a1'; find . -type d -name "build" -exec rm -rf {} +"
ssh "$USER@$REMOTE_HOST" "cd '$REMOTE_FPATH/comp-512/a1'; find . -type d -name ".gradle" -exec rm -rf {} +"

# =========== PACKAGE ===========

./gradlew :shared:clean build
./gradlew :client:installDist -PclientType=rmi
./gradlew :server:installDist -PserverType=rmi
./gradlew :middleware:installDist -PmiddlewareType=rmi

# =========== DEPLOY ===========

ssh "$USER@$REMOTE_HOST" "mkdir -p '$REMOTE_FPATH/comp-512/a1/shared/build/classes/java/main'"
scp -r ./shared/build/classes/java/main/. "$USER@$REMOTE_HOST:$REMOTE_FPATH/comp-512/a1/shared/build/classes/java/main/"

ssh "$USER@$REMOTE_HOST" "mkdir -p '$REMOTE_FPATH/comp-512/a1/client/build/install/client'"
scp -r ./client/build/install/client/. "$USER@$REMOTE_HOST:$REMOTE_FPATH/comp-512/a1/client/build/install/client/"

ssh "$USER@$REMOTE_HOST" "mkdir -p '$REMOTE_FPATH/comp-512/a1/server/build/install/server'"
scp -r ./server/build/install/server/. "$USER@$REMOTE_HOST:$REMOTE_FPATH/comp-512/a1/server/build/install/server/"

ssh "$USER@$REMOTE_HOST" "mkdir -p '$REMOTE_FPATH/comp-512/a1/middleware/build/install/middleware'"
scp -r ./middleware/build/install/middleware/. "$USER@$REMOTE_HOST:$REMOTE_FPATH/comp-512/a1/middleware/build/install/middleware/"
