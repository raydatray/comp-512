#!/bin/bash

source .env
scp -r ./build/classes/* $(USER)@mimi.cs.mcgill.ca:~/comp-512/a2/build/classes
