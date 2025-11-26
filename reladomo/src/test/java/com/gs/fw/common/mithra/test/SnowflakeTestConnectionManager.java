
/*
 Copyright 2016 Goldman Sachs.
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.
 */

package com.gs.fw.common.mithra.test;

import com.gs.fw.common.mithra.bulkloader.BulkLoader;
import com.gs.fw.common.mithra.bulkloader.BulkLoaderException;
import com.gs.fw.common.mithra.connectionmanager.XAConnectionManager;
import com.gs.fw.common.mithra.databasetype.SnowflakeDatabaseType;

public class SnowflakeTestConnectionManager extends VendorTestConnectionManager
{
    private final static SnowflakeTestConnectionManager instance = new SnowflakeTestConnectionManager();

    public static SnowflakeTestConnectionManager getInstance()
    {
        return instance;
    }

    protected SnowflakeTestConnectionManager()
    {
        readCredentials();
        connectionManager = new XAConnectionManager();
        connectionManager.setDriverClassName("net.snowflake.client.jdbc.SnowflakeDriver");
        // jdbc:snowflake://<account>.snowflakecomputing.com/
        connectionManager.setJdbcConnectionString("jdbc:snowflake://" 
            + getCredential("snowflake_account") 
            + ".snowflakecomputing.com/?db=" + getCredential("snowflake_database") 
            + "&schema=" + getCredential("snowflake_schema") 
            + "&warehouse=" + getCredential("snowflake_warehouse" ) 
            + "&CLIENT_TIMESTAMP_TYPE_MAPPING=TIMESTAMP_NTZ"
            + "&JDBC_TREAT_TIMESTAMP_NTZ_AS_UTC=false"); 
        connectionManager.setJdbcUser(getCredential("snowflake_user"));
        connectionManager.setJdbcPassword(getCredential("snowflake_password"));
        connectionManager.setDefaultSchemaName(getCredential("snowflake_schema"));
        connectionManager.setPoolName(getCredential("snowflake_account") + ":" + 
            getCredential("snowflake_database") + ":" + getCredential("snowflake_schema") + " connection pool");
        connectionManager.setInitialSize(1);
        connectionManager.setPoolSize(100);
        connectionManager.setUseStatementPooling(true);
        connectionManager.initialisePool();

        SnowflakeDatabaseType.getInstance().setTempSchema(getCredential("snowflake_schema")); 
        this.setDatabaseType(SnowflakeDatabaseType.getInstance());
    }

    public BulkLoader createBulkLoader() throws BulkLoaderException
    {
        return null; // Implement Snowflake bulk loading if needed
    }

    public String getDatabaseIdentifier()
    {
        return getCredential("snowflake_account") + getCredential("snowflake_database");
    }

    public String getSchema()
    {
        return getCredential("snowflake_schema");
    }
}