package com.arch.multitenancy.hibernate;

import org.hibernate.HibernateException;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Component
public class MultiTenantConnectionProviderImpl implements MultiTenantConnectionProvider  {

    private final DataSource dataSource;

    public MultiTenantConnectionProviderImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Connection getAnyConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void releaseAnyConnection(Connection connection) throws SQLException {
        connection.close();
    }

    @Override
    public Connection getConnection(Object o) throws SQLException {
        final Connection connection = getAnyConnection();
        try {
            connection.createStatement().execute("set search_path to " + o.toString());
        } catch (SQLException e) {
            throw new HibernateException("Não foi possível alterar para o schema " + o.toString());
        }
        return connection;
    }

    @Override
    public void releaseConnection(Object o, Connection connection) throws SQLException {
        try (connection) {
            connection.createStatement().execute("set search_path to " + o.toString());
        } catch (SQLException e) {
            throw new HibernateException("Não foi possível se conectar ao schema " + o.toString());
        }
    }

    @Override
    public boolean supportsAggressiveRelease() {
        return true;
    }

    @Override
    public boolean isUnwrappableAs(Class aClass) {
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> aClass) {
        return null;
    }
}
