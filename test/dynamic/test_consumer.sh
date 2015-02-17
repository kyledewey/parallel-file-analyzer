#!/bin/sh

cat $1 >> all.txt
cp $1 garbage/
rm $1
