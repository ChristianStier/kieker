/***************************************************************************
 * Copyright 2011 by
 *  + Christian-Albrechts-University of Kiel
 *    + Department of Computer Science
 *      + Software Engineering Group 
 *  and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ***************************************************************************/

package kieker.monitoring.writer.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import kieker.common.logging.Log;
import kieker.common.logging.LogFactory;

/**
 * @author Jan Waller
 */
public final class DBWriterHelper {
	private static final Log LOG = LogFactory.getLog(DBWriterHelper.class);

	private final Connection connection;
	private final String indexTablename;

	private final Map<Class<?>, String> createTypeMap = new ConcurrentHashMap<Class<?>, String>();

	public DBWriterHelper(final Connection connection, final String indexTablename) throws SQLException {
		this.connection = connection;
		final ResultSet databaseTypeInfo = connection.getMetaData().getTypeInfo();
		while (databaseTypeInfo.next()) {
			final int id = databaseTypeInfo.getInt("DATA_TYPE");
			final String typeName = databaseTypeInfo.getString("TYPE_NAME");
			final String typeParams = databaseTypeInfo.getString("CREATE_PARAMS");
			switch (id) {
			case Types.VARCHAR: // String
				if (typeParams != null) {
					this.createTypeMap.put(String.class, typeName + " (1024)");
				} else {
					this.createTypeMap.put(String.class, typeName);
				}
				break;
			case Types.INTEGER: // Integer
				this.createTypeMap.put(int.class, typeName);
				this.createTypeMap.put(Integer.class, typeName);
				break;
			case Types.BIGINT: // Long
				this.createTypeMap.put(long.class, typeName);
				this.createTypeMap.put(Long.class, typeName);
				break;
			case Types.REAL: // Float
				this.createTypeMap.put(float.class, typeName);
				this.createTypeMap.put(Float.class, typeName);
				break;
			case Types.DOUBLE: // Double
				this.createTypeMap.put(double.class, typeName);
				this.createTypeMap.put(Double.class, typeName);
				break;
			case Types.TINYINT: // Byte
				this.createTypeMap.put(byte.class, typeName);
				this.createTypeMap.put(Byte.class, typeName);
				break;
			case Types.SMALLINT: // Short
				this.createTypeMap.put(short.class, typeName); // NOPMD
				this.createTypeMap.put(Short.class, typeName);
				break;
			case Types.BIT: // Boolean
				this.createTypeMap.put(boolean.class, typeName);
				this.createTypeMap.put(Boolean.class, typeName);
				break;
			default: // unneeded
				break;
			}
		}
		databaseTypeInfo.close();
		this.indexTablename = indexTablename;
	}

	public void createTable(final String tablename, final Class<?>... columns) throws SQLException {
		final StringBuilder statementCreateTable = new StringBuilder();
		// FIXME: what should happen if the table already exists?
		// stmt.append("DROP TABLE ").append(tableName).append(';');
		statementCreateTable.append("CREATE TABLE ").append(tablename).append(" (id ");
		final String createLong = this.createTypeMap.get(long.class);
		if (createLong != null) {
			statementCreateTable.append(createLong);
		} else {
			throw new SQLException("Type 'long' not supported.");
		}
		int i = 1;
		for (final Class<?> c : columns) {
			statementCreateTable.append(", c").append(i++).append(' ');
			final String createType = this.createTypeMap.get(c);
			if (createType != null) {
				statementCreateTable.append(createType);
			} else {
				throw new SQLException("Type '" + c.getSimpleName() + "' not supported.");
			}
		}
		statementCreateTable.append(")");
		final String statementCreateTableString = statementCreateTable.toString();
		Statement statement = null;
		try {
			statement = this.connection.createStatement();
			if (DBWriterHelper.LOG.isDebugEnabled()) {
				DBWriterHelper.LOG.debug("Creating table: " + statementCreateTableString);
			}
			statement.execute(statementCreateTableString);
		} finally {
			if (statement != null) {
				statement.close();
			}
		}
		// insert this new table into the index table
		try {
			statement = this.connection.createStatement();
			statement.executeUpdate("INSERT INTO " + this.indexTablename + " VALUES ('" + tablename + "')");
		} finally {
			if (statement != null) {
				statement.close();
			}
		}
	}

	public void createIndexTable() throws SQLException {
		final StringBuilder statementCreateTable = new StringBuilder();
		// FIXME: what should happen if the table already exists?
		// stmt.append("DROP TABLE ").append(tableName).append(';');
		statementCreateTable.append("CREATE TABLE ").append(this.indexTablename).append(" (tables ");
		final String createString = this.createTypeMap.get(String.class);
		if (createString != null) {
			statementCreateTable.append(createString);
		} else {
			throw new SQLException("Type 'String' not supported.");
		}
		statementCreateTable.append(")");
		final String statementCreateTableString = statementCreateTable.toString();
		Statement statement = null;
		try {
			statement = this.connection.createStatement();
			if (DBWriterHelper.LOG.isDebugEnabled()) {
				DBWriterHelper.LOG.debug("Creating table: " + statementCreateTableString);
			}
			statement.execute(statementCreateTableString);
		} finally {
			if (statement != null) {
				statement.close();
			}
		}
	}

	public boolean set(final PreparedStatement preparedStatement, final int parameterIndex, final Object value) throws SQLException {
		if (value instanceof String) {
			preparedStatement.setString(parameterIndex, (String) value);
		} else if (value instanceof Integer) {
			preparedStatement.setInt(parameterIndex, (Integer) value);
		} else if (value instanceof Long) {
			preparedStatement.setLong(parameterIndex, (Long) value);
		} else if (value instanceof Float) {
			preparedStatement.setFloat(parameterIndex, (Float) value);
		} else if (value instanceof Double) {
			preparedStatement.setDouble(parameterIndex, (Double) value);
		} else if (value instanceof Byte) {
			preparedStatement.setByte(parameterIndex, (Byte) value);
		} else if (value instanceof Short) {
			preparedStatement.setShort(parameterIndex, (Short) value);
		} else if (value instanceof Boolean) {
			preparedStatement.setBoolean(parameterIndex, (Boolean) value);
		} else if (value == null) {
			DBWriterHelper.LOG.error("Null value in record not supported!");
			return false;
		} else {
			DBWriterHelper.LOG.error("Type '" + value.getClass().getSimpleName() + "' not supported");
			return false;
		}
		return true;
	}
}