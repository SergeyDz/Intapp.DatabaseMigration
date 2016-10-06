
SET mssql-connection="jdbc:sqlserver://poc-open-sql.fg.local;databaseName=POC-MANATT;user=omm;password=Tsunami9"
SET pgsql-connection="jdbc:postgresql://sdzyuban-pc.fg.local:5432/opendb?user=postgres&password=Tsunami9"

java -jar target/Intapp.DatabaseMigration-1.0-SNAPSHOT-jar-with-dependencies.jar mssql-connection=%mssql-connection% pgsql-connection=%pgsql-connection%