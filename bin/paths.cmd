reg add HKCU\Environment /v JAVA_HOME /t REG_SZ /d "C:\Program Files\Java\jre6"
reg add HKCU\Environment /v JogAmp /t REG_SZ /d "C:\grid\JOT_Core\lib\jogamp-all-platforms"
REM reg add HKCU\Environment /v GRIDGAIN_HOME /t REG_SZ /d "C:\grid\JOT_Core\lib\gridgain-4.0.2c-win"
REM reg add HKCU\Environment /v CLASSPATH /t REG_SZ /d ";C:\grid\JOT_Core\lib\jgroups\jgroups-3.2.8.Final.jar;C:\grid\JOT_Core\lib\jgroups\jgroups-sources.jar;"
reg add HKCU\Environment /v CLASSPATH /t REG_SZ /d ";C:\grid\JOT_Core\lib\jgroups\jgroups-3.2.8.Final.jar;"
reg add HKCU\Environment /v jgroups /t REG_SZ /d "C:\grid\JOT_Core\lib\jgroups"
PAUSE