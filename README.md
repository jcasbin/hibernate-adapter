# hibernate-adapter

[![codebeat badge](https://codebeat.co/badges/4bd94027-fdd2-4b11-b894-89785bf542b3)](https://codebeat.co/projects/github-com-jcasbin-hibernate-adapter-master)
[![GitHub Actions](https://github.com/jcasbin/hibernate-adapter/workflows/build/badge.svg)](https://github.com/jcasbin/hibernate-adapter/actions)
[![Coverage Status](https://coveralls.io/repos/github/jcasbin/hibernate-adapter/badge.svg?branch=master)](https://coveralls.io/github/jcasbin/hibernate-adapter?branch=master)
[![javadoc](https://javadoc.io/badge2/org.casbin/hibernate-adapter/javadoc.svg)](https://javadoc.io/doc/org.casbin/hibernate-adapter)
[![Maven Central](https://img.shields.io/maven-central/v/org.casbin/hibernate-adapter.svg)](https://mvnrepository.com/artifact/org.casbin/hibernate-adapter/latest)
[![Discord](https://img.shields.io/discord/1022748306096537660?logo=discord&label=discord&color=5865F2)](https://discord.gg/S5UjpzGZjN)

Load policy from Hibernate or save policy to it.

## Usage
First, Introduce it in the pom.xml:

```xml
<dependency>
    <groupId>org.casbin</groupId>
    <artifactId>hibernate-adapter</artifactId>
    <version>1.0.0</version>
</dependency>
```

Then you can use it in your project like this:

```java
Enforcer e = new Enforcer("examples/rbac_with_domains_model.conf");
Adapter adapter = new HibernateAdapter(DRIVER, URL, USERNAME, PASSWORD, true);
e.setAdapter(adapter);
e.loadPolicy();
```

If you use Oracle, the last entry of the parameter must be set to `true`.

## Supported Databases
Currently supported databases:

    MySQL
    Oracle
    SQL Server 2012
