#!/bin/bash

rm -rf storage
rm -f *.log
rm -f *.replay
./execute.pl -s -n Facebook -f 4 -c scripts/FbTest

