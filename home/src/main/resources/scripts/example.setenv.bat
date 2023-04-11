@echo off

set models=CONTENT                              rem Models to be dumped/restored (CONTENT, CONFIGURATION etc...)
set email=email@email.com                       rem Admin account email
set password=password                           rem Admin account password
 
set host=http://example:port                    rem URL where the VIVO instance is hosted
set purge=true                                  rem Will the restoration process purge the models before restoring
set restoration_files_path=.                    rem Directory containing the files used for restoration (restore files must be named as their corresponding models >> CONTENT.nq, CONFIGURATION.nq etc...)
set dumped_files_path=%restoration_files_path%  rem Directory containing the backed-up files
set app_name=vivo                               rem app-name parameter