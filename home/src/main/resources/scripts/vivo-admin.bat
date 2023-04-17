@echo off
 
call ./setenv.bat

set action=%1%
 
set session=%TEMP%\%RANDOM%
mkdir "%session%"
 
set url=%host%/%app_name%/authenticate
 
set login_name=%email%
set login_password=%password%
set login_form=Log in
 
:: Log in and get session cookies
curl --cookie-jar "%session%\cookies.txt" --create-dirs --data-urlencode "loginName=%login_name%" --data-urlencode "loginPassword=%login_password%" -d "loginForm=%login_form%" %url%
 
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

    curl --cookie "%session%\cookies.txt" -X POST -F "sourceFile=@%restoration_files_path%\%models%.nq" "%host%/%app_name%/dumpRestore/restore?which=%models%&purge=%purge%" > nul
 
    if %errorlevel% equ 0 (
        echo Restored successfully.
    ) else (
        echo Something went wrong.
    )
)
 
rmdir /s /q "%session%"