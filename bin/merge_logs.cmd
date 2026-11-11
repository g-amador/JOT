@echo off
cls

for /f "delims=: tokens=1" %%a in ('"dir /b | findstr log_"') do (
echo: >> results.txt
type %%a >> results.txt
)
PAUSE