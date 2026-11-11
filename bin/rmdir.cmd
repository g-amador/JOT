@echo off
cls

for /f "delims=: tokens=1" %%a in ('"dir /b | findstr log_"') do (
rmdir /S /Q %%a 
)
PAUSE