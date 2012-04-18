#!/bin/bash

rm -rf storage
rm -f *.log
rm -f *.replay
./execute.pl -s -n FileServerTester -f 4 -c scripts/ServerTest -L log

