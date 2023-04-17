#!/bin/bash

source ./setenv.sh

action="$1"

session=$(mktemp -d)

url="$host/$app_name/authenticate"

loginName="$email"
loginPassword="$password"
loginForm="Log in"

# Log in and get session cookies
curl --cookie-jar "$session/cookies.txt" --data-urlencode "loginName=$loginName" --data-urlencode "loginPassword=$loginPassword" -d "loginForm=$loginForm" $url

if [[ "$action" == "dump" ]]; then
    echo "Starting dump..."

    dump_file_path="$dumped_files_path/$models.nq"

    status_code=$(curl --write-out %{http_code} --cookie "$session/cookies.txt" "$host/$app_name/dumpRestore/dump/$models.nq?which=$models" -o "$dump_file_path")
    
    if [[ "$status_code" -ne 200 ]]; then
        echo "Dump failed, status code is $status_code, check login credentials, parameters and try again."
    fi

    if [ -s $dump_file_path ]; then
        echo "Completed successfully."
    else
        echo "Dump file is empty, deleting..."
        rm -r "$dump_file_path"
    fi

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
