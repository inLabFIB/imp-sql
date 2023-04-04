#!/bin/bash

ERRCODE=1
i=0

while [[ $i -lt 60 ]] && [[ $ERRCODE -ne 0 ]]; do
	i=$i+1
	/opt/mssql-tools/bin/sqlcmd -h -1 -t 1 -U sa -P $SA_PASSWORD -Q "SELECT 1" >> /dev/null
	ERRCODE=$?
	sleep 1
done

if [ $ERRCODE -ne 0 ]; then
	echo "Error: MSSQL SERVER took more than 60 seconds to start up."
	exit 1
fi

echo "======= MSSQL SERVER STARTED ========"

/opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P $SA_PASSWORD -d master -i /usr/src/app/sql_setups/test_db-setup.sql
/opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P $SA_PASSWORD -d master -i /usr/src/app/sql_setups/cv2-setup.sql
/opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P $SA_PASSWORD -d master -i /usr/src/app/sql_setups/tpch-setup.sql

echo "======= MSSQL CONFIG COMPLETE ======="
