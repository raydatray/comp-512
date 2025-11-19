#!/bin/bash

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
set -a
source "$SCRIPT_DIR/../.env"
set +a

scp -r ./build/classes/* "$USER@mimi.cs.mcgill.ca:/home/2023/aliu45/comp-512/a2/build/classes"
