package org.casbin.test;

import static org.junit.Assert.assertEquals;

import java.sql.SQLException;

import org.casbin.adapter.HibernateAdapter;
import org.casbin.jcasbin.main.Enforcer;
import org.junit.Test;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class HibernateAdapterDatasourceTest {

    private static final String DRIVER = "org.mariadb.jdbc.Driver";
    private static final String URL = "jdbc:mariadb://localhost:3306/casbin?serverTimezone=GMT%2B8&useSSL=false&allowPublicKeyRetrieval=true&rewriteBatchedStatements=true";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "casbin_test";
    private static HikariDataSource dataSource;
    

    @Test
    public void testInitDBfromMariaDBDatasource() throws SQLException {

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(URL);
        config.setUsername(USERNAME);
        config.setPassword(PASSWORD);
        config.setDriverClassName(DRIVER);
        config.setMaximumPoolSize(5);
        
        dataSource = new HikariDataSource(config);
        Enforcer e = new Enforcer("examples/rbac_with_domains_model.conf", new HibernateAdapter(dataSource));

        e.savePolicy(); //clear table

        e.addPolicy("admin", "domain1", "data1", "read");
        e.addGroupingPolicy("alice", "admin", "domain1");

        testDomainEnforce(e, "alice", "domain1", "data1", "read", true);
    }

    private void testDomainEnforce(Enforcer e, String sub, String dom, String obj, String act, boolean res) {
        assertEquals(res, e.enforce(sub, dom, obj, act));
    }
}
