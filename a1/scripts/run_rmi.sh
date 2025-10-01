#!/bin/bash

# this script is meant to be used on mimi
# it assumes all build artifacts exist and are ready to use

cd ..

source .env
echo "REGISTRY_PORT = $REGISTRY_PORT"
echo "FLIGHT_HOST = $FLIGHT_HOST"
echo "CAR_HOST = $CAR_HOST"
echo "ROOM_HOST = $ROOM_HOST"
echo "MIDDLEWARE_HOST = $MIDDLEWARE_HOST"

set -ex


# =========== RUN SERVERS ===========

tmux kill-session -t a1 2>/dev/null || true
tmux new-session -d -s a1 \; \
    split-window -h \; \
    split-window -v \; \
    select-pane -t 0 \; \
    split-window -v \; \
    select-layout tiled

tmux send-keys -t a1:0.1 "ssh -t ${FLIGHT_HOST} \"cd $(pwd) > /dev/null; echo -n 'Connected to '; hostname; CLASSPATH=shared/build/classes/java/main rmiregistry -J-Djava.rmi.server.useCodebaseOnly=false 1122 & ./server/build/install/server/bin/server Flights\"" C-m
tmux send-keys -t a1:0.2 "ssh -t ${CAR_HOST} \"cd $(pwd) > /dev/null; echo -n 'Connected to '; hostname; CLASSPATH=shared/build/classes/java/main rmiregistry -J-Djava.rmi.server.useCodebaseOnly=false 1122 & ./server/build/install/server/bin/server Cars\"" C-m
tmux send-keys -t a1:0.3 "ssh -t ${ROOM_HOST} \"cd $(pwd) > /dev/null; echo -n 'Connected to '; hostname; CLASSPATH=shared/build/classes/java/main rmiregistry -J-Djava.rmi.server.useCodebaseOnly=false 1122 & ./server/build/install/server/bin/server Rooms\"" C-m


# =========== RUN MIDDLEWARE ===========

tmux send-keys -t a1:0.0 "ssh -t ${MIDDLEWARE_HOST} \"cd $(pwd) > /dev/null; echo -n 'Connected to '; hostname; CLASSPATH=shared/build/classes/java/main rmiregistry -J-Djava.rmi.server.useCodebaseOnly=false 1122 & ./middleware/build/install/middleware/bin/middleware ${FLIGHT_HOST} ${CAR_HOST} ${ROOM_HOST}\"" C-m


# =========== RUN CLIENT ===========

./client/build/install/client/bin/client ${MIDDLEWARE_HOST} Middleware