# hibernate-adapter

[![codebeat badge](https://codebeat.co/badges/4bd94027-fdd2-4b11-b894-89785bf542b3)](https://codebeat.co/projects/github-com-jcasbin-hibernate-adapter-master)
[![Build Status](https://travis-ci.org/jcasbin/hibernate-adapter.svg?branch=master)](https://travis-ci.org/jcasbin/hibernate-adapter)
[![Coverage Status](https://coveralls.io/repos/github/jcasbin/hibernate-adapter/badge.svg?branch=master)](https://coveralls.io/github/jcasbin/hibernate-adapter?branch=master)
[![javadoc](https://javadoc.io/badge2/org.casbin/hibernate-adapter/javadoc.svg)](https://javadoc.io/doc/org.casbin/hibernate-adapter)
[![Maven Central](https://img.shields.io/maven-central/v/org.casbin/hibernate-adapter.svg)](https://mvnrepository.com/artifact/org.casbin/hibernate-adapter/latest)
[![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/casbin/lobby)

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
    SQL Server 2012