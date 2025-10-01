#!/bin/bash

cd ..
source .env
echo "REMOTE_USER = $REMOTE_USER"
echo "REMOTE_HOST = $REMOTE_HOST"
echo "REMOTE_FPATH = $REMOTE_FPATH"
echo "APP_TYPE = $APP_TYPE"

set -ex

# =========== CLEAN ===========

# local
./gradlew clean --refresh-dependencies --no-build-cache

# remote
ssh "$REMOTE_USER@$REMOTE_HOST" "cd '$REMOTE_FPATH/comp-512/a1'; find . -type d -name "build" -exec rm -rf {} +"
ssh "$REMOTE_USER@$REMOTE_HOST" "cd '$REMOTE_FPATH/comp-512/a1'; find . -type d -name ".gradle" -exec rm -rf {} +"

# =========== PACKAGE ===========

./gradlew :shared:clean build
./gradlew :client:installDist -PclientType=rmi
./gradlew :server:installDist -PserverType=rmi
./gradlew :middleware:installDist -PmiddlewareType=rmi

# =========== DEPLOY ===========

ssh "$REMOTE_USER@$REMOTE_HOST" "mkdir -p '$REMOTE_FPATH/comp-512/a1/shared/build/classes/java/main'"
scp -r ./shared/build/classes/java/main/. "$REMOTE_USER@$REMOTE_HOST:$REMOTE_FPATH/comp-512/a1/shared/build/classes/java/main/"

ssh "$REMOTE_USER@$REMOTE_HOST" "mkdir -p '$REMOTE_FPATH/comp-512/a1/client/build/install/client'"
scp -r ./client/build/install/client/. "$REMOTE_USER@$REMOTE_HOST:$REMOTE_FPATH/comp-512/a1/client/build/install/client/"

ssh "$REMOTE_USER@$REMOTE_HOST" "mkdir -p '$REMOTE_FPATH/comp-512/a1/server/build/install/server'"
scp -r ./server/build/install/server/. "$REMOTE_USER@$REMOTE_HOST:$REMOTE_FPATH/comp-512/a1/server/build/install/server/"

ssh "$REMOTE_USER@$REMOTE_HOST" "mkdir -p '$REMOTE_FPATH/comp-512/a1/middleware/build/install/middleware'"
scp -r ./middleware/build/install/middleware/. "$REMOTE_USER@$REMOTE_HOST:$REMOTE_FPATH/comp-512/a1/middleware/build/install/middleware/"
