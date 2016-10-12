#!/bin/bash

sqoop 'export' \
--connect jdbc:postgresql://localhost/sales_data \
--username hduser \
--password hduser \
--table browsing \
--update-key product_name \
--export-dir 'out2/' \
--driver org.postgresql.Driver \
--connection-manager org.apache.sqoop.manager.GenericJdbcManager \
--direct \
--input-fields-terminated-by '\t' \
--lines-terminated-by '\n'

sqoop 'export' \
--connect jdbc:postgresql://localhost/sales_data \
--username hduser \
--password hduser \
--table browsinghours \
--update-key hour \
--export-dir 'out4/' \
--driver org.postgresql.Driver \
--connection-manager org.apache.sqoop.manager.GenericJdbcManager \
--direct \
--input-fields-terminated-by '\t' \
--lines-terminated-by '\n'
