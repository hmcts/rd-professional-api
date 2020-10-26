#!/bin/sh

DATABASE_USER=$1;
DATABASE_NAME=$2;
DATABASE_HOST=$3;
DATABASE_PORT=$4;
FILE_NAME="delete-functional-test-data.sql"

echo "executing nightly build delete script"

psql "dbname=$DATABASE_NAME sslmode=require" -h $DATABASE_HOST -U $DATABASE_USER -p $DATABASE_PORT -f /deletescript/sql/$FILE_NAME --set AUTOCOMMIT=off

psql_exit_status=$?

if [ $psql_exit_status != 0 ]; then
    echo "psql failed while trying to run sql script" 1>&2
    exit $psql_exit_status
fi

echo "completed nightly build delete script"