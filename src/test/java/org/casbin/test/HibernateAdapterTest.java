package org.casbin.test;

import org.casbin.adapter.HibernateAdapter;
import org.casbin.jcasbin.main.Enforcer;
import org.casbin.jcasbin.persist.Adapter;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HibernateAdapterTest {
    private static final String DRIVER = "com.mysql.jdbc.Driver";
    private static final String URL = "jdbc:mysql://172.18.0.1:3306/casbin";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "casbin_test";

    @Before
    public void initDataBase() {
        Enforcer e = new Enforcer("examples/rbac_with_domains_model.conf");

        Adapter adapter = new HibernateAdapter(DRIVER, URL, USERNAME, PASSWORD, true);

        e.setAdapter(adapter);
        e.savePolicy(); //clear table

        e.addPolicy("admin", "domain2", "data2", "write");
        e.addPolicy("admin", "domain2", "data2", "read");
        e.addPolicy("admin", "domain1", "data1", "write");
        e.addPolicy("admin", "domain1", "data1", "read");

        e.addGroupingPolicy("alice", "admin", "domain1");
        e.addGroupingPolicy("bob", "admin", "domain2");
    }

    @Test
    public void testLoadPolicy() {
        Enforcer e = new Enforcer("examples/rbac_with_domains_model.conf");

        Adapter adapter = new HibernateAdapter(DRIVER, URL, USERNAME, PASSWORD, true);

        e.setAdapter(adapter);
        e.loadPolicy();

        testDomainEnforce(e, "alice", "domain1", "data1", "read", true);
        testDomainEnforce(e, "alice", "domain1", "data1", "write", true);
        testDomainEnforce(e, "alice", "domain1", "data2", "read", false);
        testDomainEnforce(e, "alice", "domain1", "data2", "write", false);
        testDomainEnforce(e, "bob", "domain2", "data1", "read", false);
        testDomainEnforce(e, "bob", "domain2", "data1", "write", false);
        testDomainEnforce(e, "bob", "domain2", "data2", "read", true);
        testDomainEnforce(e, "bob", "domain2", "data2", "write", true);
    }

    @Test
    public void testSavePolicy() {
        Enforcer e = new Enforcer("examples/rbac_with_domains_model.conf");

        Adapter adapter = new HibernateAdapter(DRIVER, URL, USERNAME, PASSWORD, true);

        e.setAdapter(adapter);
        e.loadPolicy();

        testDomainEnforce(e, "alice", "domain1", "data1", "read", true);
        testDomainEnforce(e, "alice", "domain1", "data1", "write", true);
        testDomainEnforce(e, "alice", "domain1", "data2", "read", false);
        testDomainEnforce(e, "alice", "domain1", "data2", "write", false);
        testDomainEnforce(e, "bob", "domain2", "data1", "read", false);
        testDomainEnforce(e, "bob", "domain2", "data1", "write", false);
        testDomainEnforce(e, "bob", "domain2", "data2", "read", true);
        testDomainEnforce(e, "bob", "domain2", "data2", "write", true);

        testDomainEnforce(e, "bob", "domain3", "data3", "write", false);

        e.addPolicy("admin", "domain3", "data3", "write");

        e.addGroupingPolicy("bob", "admin", "domain3");

        testDomainEnforce(e, "bob", "domain3", "data3", "write", true);
        e.savePolicy();
    }

    @Test
    public void testRemovePolicy() {
        Enforcer e = new Enforcer("examples/rbac_with_domains_model.conf");

        Adapter adapter = new HibernateAdapter(DRIVER, URL, USERNAME, PASSWORD, true);

        e.setAdapter(adapter);
        e.loadPolicy();

        e.removePolicy("admin", "domain3", "data3", "write");
        e.removeGroupingPolicy("bob", "admin", "domain3");
    }

    private void testDomainEnforce(Enforcer e, String sub, String dom, String obj, String act, boolean res) {
        assertEquals(res, e.enforce(sub, dom, obj, act));
    }
}
