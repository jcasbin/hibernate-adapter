/* Copyright 2025 happenedIn
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.casbin.test;

import static org.junit.Assert.assertEquals;
import java.sql.SQLException;
import org.casbin.adapter.HibernateAdapter;
import org.casbin.jcasbin.main.Enforcer;
import org.junit.Test;
import org.mariadb.jdbc.MariaDbDataSource;

public class HibernateAdapterDatasourceTest {

    private static final String URL = "jdbc:mariadb://localhost:3306/casbin?serverTimezone=GMT%2B8&useSSL=false&allowPublicKeyRetrieval=true&rewriteBatchedStatements=true";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "casbin_test";

    @Test
    public void testInitDBfromMariaDBDatasource() throws SQLException {
        MariaDbDataSource mariaDbDataSource = new MariaDbDataSource();
        mariaDbDataSource.setUrl(URL);
        mariaDbDataSource.setUser(USERNAME);
        mariaDbDataSource.setPassword(PASSWORD);
        
        Enforcer e = new Enforcer("examples/rbac_with_domains_model.conf", new HibernateAdapter(mariaDbDataSource));

        e.savePolicy(); //clear table

        e.addPolicy("admin", "domain1", "data1", "read");
        e.addGroupingPolicy("alice", "admin", "domain1");

        testDomainEnforce(e, "alice", "domain1", "data1", "read", true);
    }

    private void testDomainEnforce(Enforcer e, String sub, String dom, String obj, String act, boolean res) {
        assertEquals(res, e.enforce(sub, dom, obj, act));
    }
}
