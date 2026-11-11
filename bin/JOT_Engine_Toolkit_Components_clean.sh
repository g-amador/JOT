#!/bin/sh

#30-37 set foreground color 40-47 set background color
#Black       30
#Red         31
#Green       32
#Brown       33
#Blue        34
#Purple      35
#Cyan        36
#Light Gray  37

clear

echo '\033[32mCleaning...'
cd ..
rm lib/JOT-*.jar
ls lib/
echo '\033[31mDone!'
