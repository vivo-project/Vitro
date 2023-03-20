#!/bin/bash

action="$1"
models="$2"
email="$3"
password="$4"

host="http://example:port"                # URL where the VIVO instance is hosted
purge="true"                              # Will the restoration process purge the models before restoring
restoration_files_path="."                # Directory containing the files used for restoration
dumped_files_path=$restoration_files_path # Directory containing the backed-up files
app_name="vivo"                           # app-name parameter

session=$(mktemp -d)

url="$host/$app_name/authenticate"

loginName="$email"
loginPassword="$password"
loginForm="Log in"

# Log in and get session cookies
curl --cookie-jar "$session/cookies.txt" -d "loginName=$loginName" -d "loginPassword=$loginPassword" -d "loginForm=$loginForm" $url

if [[ "$action" == "dump" ]]; then
    echo "Starting dump..."
    curl --cookie "$session/cookies.txt" "$host/$app_name/dumpRestore/dump/$models.nq?which=$models" -o "$dumped_files_path/$models.nq"
    echo "Completed successfully."
elif [[ "$action" == "restore" ]]; then
    echo "Starting restoration process..."

    url="$host/$app_name/dumpRestore/restore"
    files=$(echo -n "$restoration_files_path/$models.nq")
    params=$(echo -n "which=$models&purge=$purge")

    curl --cookie "$session/cookies.txt" -X POST -F "sourceFile=@$files" "$url?$params" > /dev/null

    if [[ $? -eq 0 ]]; then
        echo "Restored successfully."
    else
        echo "Something went wrong."
    fi
fi

rm -rf "$session"
