# hibernate-adapter

Load policy from Hibernate or save policy to it.

## Usage
First, Introduce it in the pom.xml:
    
    <dependency>
        <groupId>org.casbin</groupId>
        <artifactId>hibernate-adapter</artifactId>
        <version>1.0.0</version>
    </dependency>
Then you can use it in your project like this:

    Enforcer e = new Enforcer("examples/rbac_with_domains_model.conf");
    Adapter adapter = new HibernateAdapter(DRIVER, URL, USERNAME, PASSWORD, true);
    e.setAdapter(adapter);
    e.loadPolicy();

If you use Oracle, the last entry of the parameter must be set to `true`.

## Supported Databases
Currently supported databases:

    MySQL
    Oracle