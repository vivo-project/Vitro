#!/bin/bash

models="CONTENT"                          # Models to be dumped/restored (CONTENT, CONFIGURATION etc...)
email="email@email.com"                   # Admin account email
password="password"                       # Admin account password

host="http://example:port"                # URL where the VIVO instance is hosted
purge="true"                              # Will the restoration process purge the models before restoring
restoration_files_path="."                # Directory containing the files used for restoration (restore files must be named as their corresponding models >> CONTENT.nq, CONFIGURATION.nq etc...)
dumped_files_path=$restoration_files_path # Directory containing the backed-up files
app_name="vivo"                           # app-name parameter