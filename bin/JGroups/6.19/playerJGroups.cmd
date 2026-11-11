@echo off
cls

rem get ip address and run player bot
for /f "delims=: tokens=2" %%a in ('"ipconfig | findstr IP | findstr 10."') do (
set tempip=%%a 
goto next
)
:next
set tempip=%tempip: =%
rem echo %tempip%
./player.cmd %tempip% p%tempip%2d wander