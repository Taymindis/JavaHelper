package com.github.taymindis.jh;

import java.sql.SQLException;
import java.util.ArrayList;

public interface TransactionalEvent extends Event {


    String queryOne(final String sql, Object... sqlParams) throws SQLException;

    String queryValueRowByComma(final String sql, Object... sqlParams) throws SQLException;

    String queryValueRowBySemicolon(final String sql, Object... sqlParams) throws SQLException;

    String queryValueRowByVertical(final String sql, Object... sqlParams) throws SQLException;

    ArrayList queryToList(String sql, Object... sqlParams) throws SQLException;

    ArrayList queryColumns(String sql, Object... sqlParams) throws SQLException;

    QueryResult query(final String sql, Object... sqlParams) throws SQLException;

    int execute(String sql, Object... sqlParams) throws SQLException;

    /**
     * @param sql sql
     * @param sqlParams sqlParams
     * @return row affected
     * @throws SQLException SQLException
     */
    int executeWithKey(String sql, Object... sqlParams) throws SQLException;

    void rollback() throws SQLException;

    void commit() throws SQLException;

    void release() throws SQLException;

    @Override
    <T> T getResult();

}