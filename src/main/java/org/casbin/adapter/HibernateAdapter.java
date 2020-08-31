package org.casbin.adapter;

import org.apache.commons.collections.CollectionUtils;
import org.casbin.jcasbin.model.Assertion;
import org.casbin.jcasbin.model.Model;
import org.casbin.jcasbin.persist.Adapter;
import org.casbin.jcasbin.persist.Helper;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import java.util.*;

public class HibernateAdapter implements Adapter {
    private String driver;
    private String url;
    private String username;
    private String password;
    private int size = 0;
    private boolean dbSpecified;
    private SessionFactory factory;

    public HibernateAdapter(String driver, String url, String username, String password) {
        this(driver, url, username, password, false);
    }

    public HibernateAdapter(String driver, String url, String username, String password, boolean dbSpecified) {
        this.driver = driver;
        this.url = url;
        this.username = username;
        this.password = password;
        this.dbSpecified = dbSpecified;

        open();
    }

    private void open() {
        this.factory = initSessionFactory();
        if (!this.dbSpecified) {
            createDatabase();
            this.factory = initSessionFactory();
        }
        createTable();
    }

    private void createDatabase() {
        Session session = factory.openSession();
        Transaction tx = session.beginTransaction();
        if (this.driver.contains("mysql")) {
            session.createSQLQuery("CREATE DATABASE IF NOT EXISTS casbin").executeUpdate();
            session.createSQLQuery("USE casbin").executeUpdate();
        } else if (this.driver.contains("sqlserver")) {
            session.createSQLQuery("IF NOT EXISTS (" +
                    "SELECT * FROM sysdatabases WHERE name = 'casbin') CREATE DATABASE casbin ON PRIMARY " +
                    "( NAME = N'casbin', FILENAME = N'C:\\Program Files\\Microsoft SQL Server\\MSSQL.1\\MSSQL\\DATA\\casbinDB.mdf' , SIZE = 3072KB , MAXSIZE = UNLIMITED, FILEGROWTH = 1024KB ) " +
                    "LOG ON\n" +
                    "( NAME = N'casbin_log', FILENAME = N'C:\\Program Files\\Microsoft SQL Server\\MSSQL.1\\MSSQL\\DATA\\casbinDB_log.ldf' , SIZE = 1024KB , MAXSIZE = 2048GB , FILEGROWTH = 10%) " +
                    "COLLATE Chinese_PRC_CI_AS").executeUpdate();
            session.createSQLQuery("USE casbin").executeUpdate();
        }
        tx.commit();
        session.close();
    }

    private void createTable() {
        Session session = factory.openSession();
        Transaction tx = session.beginTransaction();
        if (this.driver.contains("mysql")) {
            session.createSQLQuery("CREATE TABLE IF NOT EXISTS casbin_rule (" +
                    "id INT not NULL primary key," +
                    "ptype VARCHAR(100) not NULL," +
                    "v0 VARCHAR(100)," +
                    "v1 VARCHAR(100)," +
                    "v2 VARCHAR(100)," +
                    "v3 VARCHAR(100)," +
                    "v4 VARCHAR(100)," +
                    "v5 VARCHAR(100))").executeUpdate();
        } else if (this.driver.contains("oracle")) {
            session.createSQLQuery("declare " +
                    "nCount NUMBER;" +
                    "v_sql LONG;" +
                    "begin " +
                    "SELECT count(*) into nCount FROM USER_TABLES where table_name = 'casbin_rule';" +
                    "IF (nCount <= 0) " +
                    "THEN " +
                    "v_sql:='" +
                    "CREATE TABLE casbin_rule " +
                    "                    (id NUMBER(10, 0) not NULL," +
                    "                     ptype VARCHAR(100) not NULL, " +
                    "                     v0 VARCHAR(100), " +
                    "                     v1 VARCHAR(100), " +
                    "                     v2 VARCHAR(100), " +
                    "                     v3 VARCHAR(100)," +
                    "                     v4 VARCHAR(100)," +
                    "                     v5 VARCHAR(100))';" +
                    "execute immediate v_sql;" +
                    "END IF;" +
                    "end;").executeUpdate();
        } else if (this.driver.contains("sqlserver")) {
            session.createSQLQuery("if not exists (select * from sysobjects where id = object_id('casbin_rule')) " +
                    "create table  casbin_rule (" +
                    "   id int, " +
                    "   ptype VARCHAR(100) , " +
                    "   v0 VARCHAR(100), " +
                    "   v1 VARCHAR(100), " +
                    "   v2 VARCHAR(100), " +
                    "   v3 VARCHAR(100), " +
                    "   v4 VARCHAR(100), " +
                    "   v5 VARCHAR(100), " +
                    "   primary key (id) " +
                    ")").executeUpdate();
        }
        tx.commit();
        session.close();
    }

    private void dropTable() {
        Session session = factory.openSession();
        Transaction tx = session.beginTransaction();
        if (this.driver.contains("mysql")) {
            session.createSQLQuery("DROP TABLE IF EXISTS casbin_rule").executeUpdate();
        } else if (this.driver.contains("oracle")) {
            session.createSQLQuery("declare " +
                    "nCount NUMBER;" +
                    "v_sql LONG;" +
                    "begin " +
                    "SELECT count(*) into nCount FROM dba_tables where table_name = 'casbin_rule';" +
                    "IF(nCount >= 1) " +
                    "THEN " +
                    "v_sql:='drop table casbin_rule';" +
                    "execute immediate v_sql;" +
                    "END IF;" +
                    "end;").executeUpdate();
        } else if (this.driver.contains("sqlserver")) {
            session.createSQLQuery("if exists (select * from sysobjects where id = object_id('casbin_rule') drop table casbin_rule").executeUpdate();
        }
        tx.commit();
        session.close();
    }

    private SessionFactory initSessionFactory() {
        Configuration configuration = new Configuration();
        Properties properties = new Properties();
        properties.setProperty("hibernate.connection.driver_class", this.driver);
        properties.setProperty("hibernate.connection.url", this.url);
        properties.setProperty("hibernate.connection.username", this.username);
        properties.setProperty("hibernate.connection.password", this.password);
        if (this.driver.contains("mysql")) {
            properties.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQL57Dialect");
        } else if (this.driver.contains("oracle")) {
            properties.setProperty("hibernate.dialect", "org.hibernate.dialect.Oracle9iDialect");
        } else if (this.driver.contains("sqlserver")) {
            properties.setProperty("hibernate.dialect", "org.hibernate.dialect.SQLServer2012Dialect");
        }
        configuration.setProperties(properties);

        configuration.addClass(CasbinRule.class);

        return configuration.buildSessionFactory();
    }

    @Override
    public void loadPolicy(Model model) {
        Session session = factory.openSession();
        Transaction tx = session.beginTransaction();
        List<CasbinRule> casbinRules = session.createSQLQuery("SELECT * FROM casbin_rule").addEntity(CasbinRule.class).list();
        for (CasbinRule line : casbinRules) {
            loadPolicyLine(line, model);
        }
        size = casbinRules.size();
        tx.commit();
        session.close();
    }

    private static void loadPolicyLine(CasbinRule line, Model model){
        String lineText = line.getPtype();
        if (line.getV0() != null) {
            lineText += ", " + line.getV0();
        }
        if (line.getV1() != null) {
            lineText += ", " + line.getV1();
        }
        if (line.getV2() != null) {
            lineText += ", " + line.getV2();
        }
        if (line.getV3() != null) {
            lineText += ", " + line.getV3();
        }
        if (line.getV4() != null) {
            lineText += ", " + line.getV4();
        }
        if (line.getV5() != null) {
            lineText += ", " + line.getV5();
        }
        Helper.loadPolicyLine(lineText,model);
    }

    @Override
    public void savePolicy(Model model) {
        dropTable();
        createTable();
        Session session = factory.openSession();
        Transaction tx = session.beginTransaction();
        size = 0;
        for (Map.Entry<String, Assertion> entry : model.model.get("p").entrySet()) {
            String ptype = entry.getKey();
            Assertion ast = entry.getValue();
            for (List<String> rule : ast.policy) {
                size++;
                CasbinRule line = savePolicyLine(ptype, rule, size);
                insertData(line, session);
            }
        }
        for (Map.Entry<String, Assertion> entry : model.model.get("g").entrySet()) {
            String ptype = entry.getKey();
            Assertion ast = entry.getValue();
            for (List<String> rule : ast.policy) {
                size++;
                CasbinRule line = savePolicyLine(ptype, rule, size);
                insertData(line, session);
            }
        }
        tx.commit();
        session.close();
    }

    private void insertData(CasbinRule line, Session session) {
        String sql = String.format("INSERT INTO casbin_rule (id,ptype,v0,v1,v2,v3,v4,v5) VALUES (%d, '%s','%s','%s','%s','%s','%s','%s')",
                line.getId(),
                line.getPtype(),
                line.getV0(),
                line.getV1(),
                line.getV2(),
                line.getV3(),
                line.getV4(),
                line.getV5());
        session.createSQLQuery(sql).executeUpdate();
        session.createSQLQuery("UPDATE casbin_rule SET v1 = null WHERE v1 = 'null'").executeUpdate();
        session.createSQLQuery("UPDATE casbin_rule SET v2 = null WHERE v2 = 'null'").executeUpdate();
        session.createSQLQuery("UPDATE casbin_rule SET v3 = null WHERE v3 = 'null'").executeUpdate();
        session.createSQLQuery("UPDATE casbin_rule SET v4 = null WHERE v4 = 'null'").executeUpdate();
        session.createSQLQuery("UPDATE casbin_rule SET v5 = null WHERE v5 = 'null'").executeUpdate();
    }

    private void deleteData(Session session, String ptype, List<String> rules) {
        StringBuilder sql = new StringBuilder("DELETE FROM casbin_rule WHERE ptype = '" + ptype + "'");
        for (int i=0;i<rules.size();i++) {
            sql.append(" AND v").append(i).append(" = '").append(rules.get(i)).append("'");
        }
        session.createSQLQuery(sql.toString()).executeUpdate();
    }

    private CasbinRule savePolicyLine(String ptype, List<String> rule, int id) {
        CasbinRule line = new CasbinRule();
        line.setId(id);
        line.setPtype(ptype);
        if (rule.size() > 0) {
            line.setV0(rule.get(0));
        }
        if (rule.size() > 1) {
            line.setV1(rule.get(1));
        }
        if (rule.size() > 2) {
            line.setV2(rule.get(2));
        }
        if (rule.size() > 3) {
            line.setV3(rule.get(3));
        }
        if (rule.size() > 4) {
            line.setV4(rule.get(4));
        }
        if (rule.size() > 5) {
            line.setV5(rule.get(5));
        }

        return line;
    }

    @Override
    public void addPolicy(String sec, String ptype, List<String> rule) {
        if(CollectionUtils.isEmpty(rule)) return;
        CasbinRule line = savePolicyLine(ptype, rule, ++size);
        Session session = factory.openSession();
        Transaction tx = session.beginTransaction();
        insertData(line, session);
        tx.commit();
        session.close();
    }

    @Override
    public void removePolicy(String sec, String ptype, List<String> rule) {
        if(CollectionUtils.isEmpty(rule)) return;
        removeFilteredPolicy(sec, ptype, 0, rule.toArray(new String[0]));
    }

    @Override
    public void removeFilteredPolicy(String sec, String ptype, int fieldIndex, String... fieldValues) {
        List<String> values = Optional.of(Arrays.asList(fieldValues)).orElse(new ArrayList<>());
        if(CollectionUtils.isEmpty(values)) return;
        Session session = factory.openSession();
        Transaction tx = session.beginTransaction();
        deleteData(session, ptype, values);
        tx.commit();
        session.close();
        reset();
    }

    private void reset() {
        Session session = factory.openSession();
        Transaction tx = session.beginTransaction();
        List<CasbinRule> casbinRules = session.createSQLQuery("SELECT * FROM casbin_rule").addEntity(CasbinRule.class).list();
        tx.commit();
        session.close();

        dropTable();
        createTable();
        for (int i=1;i<=casbinRules.size();i++) {
            casbinRules.get(i - 1).setId(i);
        }
        this.size = casbinRules.size();

        session = factory.openSession();
        tx = session.beginTransaction();
        for (CasbinRule rule : casbinRules) {
            insertData(rule, session);
        }
        tx.commit();
        session.close();
    }

    public int getPolicySize() {
        return size;
    }
}
