@echo off
cls
set p/ firstline=dir /b jgroups-*
java -jar %firstline%
PAUSE
