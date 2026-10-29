@echo off
setlocal EnableDelayedExpansion

set "SCRIPT_DIR=%~dp0"
set "OUT_DIR=%SCRIPT_DIR%out"
mkdir "%OUT_DIR%" 2>nul

set "JAVA_BIN="

if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\java.exe" if exist "%JAVA_HOME%\bin\javac.exe" (
        set "JAVA_BIN=%JAVA_HOME%\bin"
    )
)

if "%JAVA_BIN%"=="" (
    for %%D in (
        "C:\Program Files\Java\jdk1.8.0_251\bin"
        "C:\Program Files\Java\jdk-17\bin"
        "C:\Program Files\Java\jdk-21\bin"
        "C:\Program Files\Java\bin"
    ) do (
        if exist "%%~D\java.exe" if exist "%%~D\javac.exe" (
            set "JAVA_BIN=%%~D"
            goto :DetectedJava
        )
    )
)

if "%JAVA_BIN%"=="" (
    where java >nul 2>nul
    if not errorlevel 1 (
        for /f "delims=" %%I in ('where java 2^>nul') do (
            set "JAVA_BIN=%%~dpI"
            if exist "%%~dpI\javac.exe" goto :DetectedJava
        )
    )
)

if "%JAVA_BIN%"=="" (
    echo Java JDK was not found. Please install Java 8+ and ensure java and javac are available.
    exit /b 1
)

:DetectedJava
if not exist "%JAVA_BIN%\javac.exe" (
    echo Java JDK was not found. Please install Java 8+ and ensure javac is available in the JDK.
    exit /b 1
)

set "SOURCE_FILES="
for %%F in ("%SCRIPT_DIR%src\*.java") do (
    set "SOURCE_FILES=!SOURCE_FILES! "%%~fF""
)

"%JAVA_BIN%\javac.exe" -d "%OUT_DIR%" %SOURCE_FILES%
if errorlevel 1 exit /b %errorlevel%

"%JAVA_BIN%\java.exe" -cp "%OUT_DIR%" Main
exit /b %errorlevel%
