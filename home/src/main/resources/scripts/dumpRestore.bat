@echo off

set action=%1%
set models=%2%
set email=%3%
set password=%4%

set host=http://example:port
set purge=true
set restoration_files=.
set dumped_files=.
set app_name=vivo

set session=%TEMP%\%RANDOM%
mkdir "%session%"

set url=%host%/%app_name%/authenticate

set loginName=%email%
set loginPassword=%password%
set loginForm=Log in

:: Log in and get session cookies
curl --cookie-jar "%session%\cookies.txt" --create-dirs -d "loginName=%loginName%" -d "loginPassword=%loginPassword%" -d "loginForm=%loginForm%" %url%

if "%action%" == "dump" (
    echo Starting dump...
    
    curl --cookie "%session%\cookies.txt" "%host%/%app_name%/dumpRestore/dump/%models%.nq?which=%models%" -o "%dumped_files%\%models%.nq"
    
    echo Completed successfully.
) else if "%action%" == "restore" (
    echo Starting restoration process...

    set url=%host%/%app_name%/dumpRestore/restore

    curl --cookie "%session%\cookies.txt" -X POST -F "sourceFile=@%restoration_files%\%models%.nq" "%url%?which=%models%&purge=%purge%" > nul

    if %errorlevel% equ 0 (
        echo Restored successfully.
    ) else (
        echo Something went wrong.
    )
)

rmdir /s /q "%session%"
