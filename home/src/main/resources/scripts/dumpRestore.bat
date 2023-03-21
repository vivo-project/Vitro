@echo off
 
set action=%1%
set models=%2%
set email=%3%
set password=%4%
 
set host=http://example:port
set purge=true
set restoration_files_path=.
set dumped_files_path=%restoration_files_path%
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
 
    for /f "tokens=* usebackq" %%F in (`curl --silent -w %%{http_code}%% --cookie "%session%\cookies.txt" "%host%/%app_name%/dumpRestore/dump/%models%.nq?which=%models%" -o "%dumped_files_path%\%models%.nq"`) do ( 
        if "%%F" == "302%%" (
            echo "Dump failed, status code is %%F. check login credentials, parameters and try again."
        )
    )
 
    for %%A in ("%dumped_files_path%\%models%.nq") do (
        if %%~zA equ 0 (
            echo "Dump file is empty, deleting..."
            del "%dumped_files_path%\%models%.nq"
        ) else (
            echo "Completed successfully."
        )
    )
 
) else if "%action%" == "restore" (
    echo Starting restoration process...
 
    set url=%host%/%app_name%/dumpRestore/restore
 
    curl --cookie "%session%\cookies.txt" -X POST -F "sourceFile=@%restoration_files_path%\%models%.nq" "%url%?which=%models%&purge=%purge%" > nul
 
    if %errorlevel% equ 0 (
        echo Restored successfully.
    ) else (
        echo Something went wrong.
    )
)
 
rmdir /s /q "%session%"