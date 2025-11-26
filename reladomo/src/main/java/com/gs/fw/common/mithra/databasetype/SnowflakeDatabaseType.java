/*
 * Copyright 2016 Goldman Sachs.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.gs.fw.common.mithra.databasetype;

import com.gs.fw.common.mithra.MithraObjectPortal;
import com.gs.fw.common.mithra.attribute.Attribute;
import com.gs.fw.common.mithra.attribute.update.AttributeUpdateWrapper;
import com.gs.fw.common.mithra.finder.SqlQuery;
import com.gs.fw.common.mithra.tempobject.TupleTempContext;
import com.gs.fw.common.mithra.util.MithraFastList;
import com.gs.fw.common.mithra.util.TableColumnInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

// No row-level locks for standard Snowflake tables.
// Future: add optional optimistic checks (version or compare-on-change) in UPDATE...FROM join.
public class SnowflakeDatabaseType extends AbstractDatabaseType
{
    private static final Logger logger = LoggerFactory.getLogger(SnowflakeDatabaseType.class.getName());
    private static final String DUPLICATE_ERROR_CODE = "23505";
    private static final char[] SNOWFLAKE_SQL_META_CHARS = { '%', '_', '\\' };
    private static final SnowflakeDatabaseType instance = new SnowflakeDatabaseType();
    private static final Map<String, String> sqlToJavaTypes;

    private String tempSchema = null;
    private String database = null;

    enum OptimisticMode 
    {
        NONE, VERSION_COLUMN, COMPARE_COLUMNS
    }

    public void setDatabase(String database)
    {
        this.database = database;
    }
    // private OptimisticMode optimisticMode = OptimisticMode.NONE; // future use

    public static Logger getLogger()
    {
        return logger;
    }

    static
    {
        // https://docs.snowflake.com/en/developer-guide/udf-stored-procedure-data-type-mapping
        Map<String, String> m = new HashMap<String, String>();

        m.put("decimal", "BigDecimal");
        m.put("numeric", "BigDecimal");

        m.put("float", "Double");
        m.put("double", "Double");
        m.put("double precision", "Double");
        m.put("boolean", "Boolean");

        m.put("varchar", "String");
        m.put("string", "String");
        m.put("text", "String");
        m.put("binary", "byte[]");

        m.put("date", "Date");
        m.put("time", "Time");
        m.put("timestamp", "Timestamp");
        m.put("timestamp_ntz", "Timestamp");
        m.put("timestamp_ltz", "Timestamp");
        m.put("timestamp_tz", "Timestamp");

        sqlToJavaTypes = Collections.unmodifiableMap(m);
    }

    public static SnowflakeDatabaseType getInstance()
    {
        return instance;
    }

    protected SnowflakeDatabaseType()
    {
    }

    @Override
    public int getMaxPreparedStatementBatchCount(int parametersPerStatement)
    {
        return 100;
    }

    public boolean hasTopQuery()
    {
        return true;
    }

    public void setTempSchema(String tempSchema)
    {
        this.tempSchema = tempSchema;
    }

    @Override
    public String convertDateToString(java.util.Date date)
    {
        return super.convertDateToString(date);
    }

    // --- SELECT ---

    @Override
    public String getSelect(String columns, SqlQuery query, String groupBy, boolean isInTransaction, int rowCount)
    {
        String result = super.getSelect(columns, query, groupBy, isInTransaction, rowCount);
        if (rowCount > 0)
        {
            result += " LIMIT " + (rowCount + 1);
        }
        // Intentionally no "FOR UPDATE" for Snowflake Standard Tables.
        return result;
    }

    @Override
    public String getSelect(String columns, String fromClause, String whereClause, boolean lock)
    {
        // Ignore "lock" for Snowflake Standard Tables.
        return super.getSelect(columns, fromClause, whereClause, lock);
    }

    /**
     * Snowflake-specific tweak for aggregate queries.
     *
     * Background:
     * - For integer inputs Snowflake's VAR_POP / VARIANCE / AVG return a FIXED/NUMBER(p,s)
     *   value with a non-zero scale (e.g. 2.000000).
     * - Mithra aggregate attributes for var/variance/stddev/avg over byte/int/long
     *   are mapped to primitive types and populated via ResultSet.getInt()/getLong()/getDouble().
     * - The Snowflake JDBC driver does not coerce scaled FIXED values to int/long and throws
     *   "Cannot convert value in the driver from type: FIXED(..) to type: Int/Long".
     *
     * To keep the Snowflake adapter compatible with existing schemas and tests, we cast the
     * aggregate inputs to DOUBLE so that VAR_POP / VARIANCE / AVG also return DOUBLE, which
     * the driver can safely read through the existing Mithra code paths.
     */
    @Override
    public String getSelectForAggregatedData(SqlQuery query, List aggregateAttributes, List groupByAttributes)
    {
        String sql = super.getSelectForAggregatedData(query, aggregateAttributes, groupByAttributes);

        sql = sql.replaceAll(
            "(?i)\\bvar_pop\\s*\\(([^)]+)\\)",
            "VAR_POP(CAST($1 AS DOUBLE))");

        sql = sql.replaceAll(
            "(?i)\\bvariance\\s*\\(([^)]+)\\)",
            "VARIANCE(CAST($1 AS DOUBLE))");

        sql = sql.replaceAll(
            "(?i)\\bavg\\s*\\(([^)]+)\\)",
            "CAST(AVG(CAST($1 AS DOUBLE)) AS DOUBLE)");

        return sql;
    }

    // --- DELETE ---

    @Override
    public String getDelete(SqlQuery query, int rowCount)
    {
        // Snowflake does NOT support "DELETE ... LIMIT n" directly.
        // We fall back to deleting by WHERE only when rowCount>0 is requested.
        StringBuilder sb = new StringBuilder("delete from ").append(query.getFromClauseAsString());
        String where = query.getWhereClauseAsString(0);
        if (where != null && where.trim().length() > 0)
        {
            sb.append(" where ").append(where);
        }
        // If you need limited delete, implement a PK-driven EXISTS with subselect
        // LIMIT.
        return sb.toString();
    }

    // --- locking / tx / retries ---

    @Override
    public int zGetTxLevel()
    {
        return Connection.TRANSACTION_READ_COMMITTED;
    }

    @Override
    public String getPerStatementLock(boolean lock)
    {
        return "";
    }

    @Override
    protected boolean hasRowLevelLocking()
    {
        return false;
    }

    @Override
    public boolean dropTableAllowedInTransaction()
    {
        return false;
    }

    @Override
    protected boolean isRetriableWithoutRecursion(SQLException sqlException)
    {
        return false;
    }

    @Override
    protected boolean isTimedOutWithoutRecursion(SQLException exception)
    {
        return false;
    }

    @Override
    public boolean violatesUniqueIndexWithoutRecursion(SQLException exception)
    {
        return DUPLICATE_ERROR_CODE.equals(exception.getSQLState());
    }

    // --- temp tables ---

    @Override
    public String getSqlPrefixForNonSharedTempTableCreation(String nominalTableName)
    {
        return "create temporary table " + nominalTableName + getSqlPostfixForNonSharedTempTableCreation();
    }

    @Override
    public String getSqlPostfixForNonSharedTempTableCreation()
    {
        return "";
    }

    @Override
    public String getSqlPostfixForSharedTempTableCreation()
    {
        return "";
    }

    @Override
    public String appendNonSharedTempTableCreatePreamble(StringBuilder sb, String tempTableName)
    {
        sb.append("CREATE TEMPORARY TABLE ").append(tempTableName);
        return tempTableName;
    }

    @Override
    public String appendSharedTempTableCreatePreamble(StringBuilder sb, String tempTableName)
    {
        if (tempSchema != null)
        {
            tempTableName = tempSchema + "." + tempTableName;
        }
        sb.append("CREATE TABLE ").append(tempTableName);
        return tempTableName;
    }

    @Override
    public boolean indexRequiresSchemaName()
    {
        return false;
    }

    @Override
    public boolean nonSharedTempTablesAreDroppedAutomatically()
    {
        return true;
    }

    // --- SQL data types ---

    @Override
    public String getSqlDataTypeForBoolean()
    {
        return "boolean";
    }

    // default to NTZ; adjust to LTZ/TZ if needed in schema generation
    @Override
    public String getSqlDataTypeForTimestamp()
    {
        return "timestamp_ntz";
    }

    @Override
    public String getSqlDataTypeForTime()
    {
        return "time";
    }

    @Override
    public String getSqlDataTypeForTinyInt()
    {
        return "number(3,0)";
    }

    @Override
    public String getSqlDataTypeForVarBinary()
    {
        return "binary";
    }

    @Override
    public String getSqlDataTypeForByte()
    {
        return "number(3,0)";
    }

    @Override
    public String getSqlDataTypeForChar()
    {
        return "varchar(1)";
    }

    @Override
    public String getSqlDataTypeForDateTime()
    {
        return "date";
    }

    @Override
    public String getSqlDataTypeForDouble()
    {
        return "double";
    }

    @Override
    public String getSqlDataTypeForFloat()
    {
        return "float";
    }

    @Override
    public String getSqlDataTypeForInt()
    {
        return "number(10,0)";
    }

    @Override
    public String getSqlDataTypeForLong()
    {
        return "number(38,0)";
    }

    @Override
    public String getSqlDataTypeForShortJava()
    {
        return "number(5,0)";
    }

    @Override
    public String getSqlDataTypeForString()
    {
        return "varchar";
    }

    @Override
    public String getSqlDataTypeForBigDecimal()
    {
        return "number";
    }

    // --- schema / metadata ---

    @Override
    public String getCreateSchema(String schema)
    {
        if (schema == null || schema.trim().length() == 0)
        {
            return null;
        }
        StringBuilder sql = new StringBuilder();
        if (database != null) {
            sql.append("USE DATABASE ").append(database).append("; ");
        }
        sql.append("CREATE SCHEMA IF NOT EXISTS ").append(schema).append("; ");
        sql.append("USE SCHEMA ").append(schema);
        return sql.toString();
    }

    @Override
    public TableColumnInfo getTableColumnInfo(Connection connection, String schema, String table) throws SQLException
    {
        return super.getTableColumnInfo(connection, schema, table);
    }

    /**
     * Maps a Snowflake SQL type to a Java type.
     *
     * <p>
     * Design principles:
     * <ul>
     * <li>Always return boxed types (Integer, Long, Double, Boolean, Character)
     * because Snowflake columns are nullable by default.</li>
     * <li>Use BigDecimal for any fractional numbers or very large integers to avoid
     * precision loss.</li>
     * <li>Semi-structured types (VARIANT, ARRAY, OBJECT, MAP, GEOGRAPHY) are
     * represented as String (JSON text) by default.</li>
     * </ul>
     *
     * <p>
     * Other mappings (DATE, TIME, TIMESTAMP, etc.) are resolved from
     * {@code sqlToJavaTypes}. Unknown types default to String.
     */
    @Override
    public String getJavaTypeFromSql(String sql, Integer precision, Integer decimal)
    {
        if (sql == null)
            return null;

        String key = sql.trim().toLowerCase(Locale.ROOT);
        int idx = key.indexOf('(');
        if (idx >= 0)
        {
            key = key.substring(0, idx).trim();
        }

        String mapped = sqlToJavaTypes.get(key);
        if (mapped != null && !"BigDecimal".equals(mapped))
        {
            return mapped;
        }

        if ("number".equals(key) || "numeric".equals(key) || "decimal".equals(key))
        {
            if (decimal != null && decimal > 0)
            {
                return "BigDecimal";
            }
            if (precision != null)
            {
                if (precision <= 9)
                    return "Integer";
                if (precision <= 18)
                    return "Long";
            }
            return "BigDecimal";
        }

        if ("float".equals(key) || "double".equals(key) || "double precision".equals(key))
        {
            return "Double";
        }

        if ("char".equals(key) || "character".equals(key) || "nchar".equals(key))
        {
            if (precision != null && precision == 1)
            {
                return "Character";
            }
            return "String";
        }

        if ("variant".equals(key) || "array".equals(key) || "object".equals(key) || "map".equals(key)
                || "geography".equals(key))
        {
            return "String";
        }

        return (mapped != null) ? mapped : "String";
    }

    // --- multi-insert / identity ---

    @Override
    protected boolean hasSelectUnionMultiInsert()
    {
        return false;
    }

    @Override
    protected boolean hasValuesMultiInsert()
    {
        return false;
    }

    @Override
    public int getMultiInsertBatchSize(int columnsToInsert)
    {
        return 1000;
    }

    @Override
    public String getLastIdentitySql(String tableName)
    {
        return null;
    }

    // --- stats / conversion / like ---

    @Override
    public String getUpdateTableStatisticsSql(String tableName)
    {
        return null;
    }

    @Override
    protected char[] getLikeMetaChars()
    {
        return SNOWFLAKE_SQL_META_CHARS;
    }

    @Override
    public String getConversionFunctionIntegerToString(String expression)
    {
        return "cast(" + expression + " as varchar)";
    }

    @Override
    public String getConversionFunctionStringToInteger(String expression)
    {
        return "cast(" + expression + " as number(38,0))";
    }

    // --- date/timestamp extract ---

    @Override
    public String getSqlExpressionForDateYear(String columnName)
    {
        return "EXTRACT(YEAR FROM " + columnName + ")";
    }

    @Override
    public String getSqlExpressionForDateMonth(String columnName)
    {
        return "EXTRACT(MONTH FROM " + columnName + ")";
    }

    @Override
    public String getSqlExpressionForDateDayOfMonth(String columnName)
    {
        return "EXTRACT(DAY FROM " + columnName + ")";
    }

    @Override
    protected String getSqlExpressionForTimestampYearWithConversion(String columnName, int conversion,
            TimeZone dbTimeZone)
    {
        return "EXTRACT(YEAR FROM " + columnName + ")";
    }

    @Override
    protected String getSqlExpressionForTimestampMonthWithConversion(String columnName, int conversion,
            TimeZone dbTimeZone)
    {
        return "EXTRACT(MONTH FROM " + columnName + ")";
    }

    @Override
    protected String getSqlExpressionForTimestampDayOfMonthWithConversion(String columnName, int conversion,
            TimeZone dbTimeZone)
    {
        return "EXTRACT(DAY FROM " + columnName + ")";
    }

    // --- UPDATE ... FROM join ---

    @Override
    public void setMultiUpdateViaJoinQuery(Object source, List updates, Attribute[] prototypeArray,
            MithraFastList<Attribute> nullAttributes, int pkAttributeCount, TupleTempContext tempContext,
            MithraObjectPortal mithraObjectPortal, String fullyQualifiedTableNameGenericSource, StringBuilder builder)
    {
        this.startUpdateViaJoinQuery(fullyQualifiedTableNameGenericSource, builder);
        builder.append(" t0 set ");
        for (int i = 0; i < updates.size(); i++)
        {
            AttributeUpdateWrapper wrapper = (AttributeUpdateWrapper) updates.get(i);
            if (i > 0)
            {
                builder.append(", ");
            }
            builder.append(wrapper.getSetAttributeSql());
        }
        builder.append(" from ");
        this.appendTempTableRightSideJoin(source, prototypeArray, nullAttributes, pkAttributeCount, tempContext,
                mithraObjectPortal, builder);
    }

    @Override
    public void setBatchUpdateViaJoinQuery(Object source, List updates, Attribute[] prototypeArray,
            MithraFastList<Attribute> nullAttributes, int pkAttributeCount, TupleTempContext tempContext,
            MithraObjectPortal mithraObjectPortal, String fullyQualifiedTableNameGenericSource, StringBuilder builder)
    {
        this.startUpdateViaJoinQuery(fullyQualifiedTableNameGenericSource, builder);
        builder.append(" t0 set ");
        for (int i = 0; i < updates.size(); i++)
        {
            AttributeUpdateWrapper wrapper = (AttributeUpdateWrapper) updates.get(i);
            if (i > 0)
            {
                builder.append(", ");
            }
            builder.append(wrapper.getAttribute().getColumnName()).append(" = t1.c");
            builder.append(pkAttributeCount + i);
        }
        builder.append(" from ");
        appendTempTableRightSideJoin(source, prototypeArray, nullAttributes, pkAttributeCount, tempContext,
                mithraObjectPortal, builder);
    }

    @Override
    public boolean shouldCreateTestIndexes()
    {
        return false;
    }

}
