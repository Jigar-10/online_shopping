@echo off
setlocal
echo =====================================================================
echo  Online Shopping DBMS Desktop Studio (Java Swing + JDBC + MySQL 8.0)
echo =====================================================================
echo.

if exist "target\online-shopping-system-1.0.0.jar" (
    echo Launching pre-built executable JAR...
    java -jar target\online-shopping-system-1.0.0.jar
) else (
    echo Building and launching with Maven...
    call mvn.cmd compile exec:java
)

if errorlevel 1 (
    echo.
    echo Application exited with an error. Check logs in logs/application.log
    pause
)
