MedSavant Shard

MedSavant Shard module contains most of the functionality related to sharding including the configuration for storing variants in multiple databases. The configuration can be found in medsavant-shard/src/main/resources/hibernate*.cfg.xml files and has the following form:

<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE hibernate-configuration PUBLIC "-//Hibernate/Hibernate Configuration DTD 3.0//EN"
        "http://hibernate.sourceforge.net/hibernate-configuration-3.0.dtd">
<hibernate-configuration>
  <session-factory>
    <property name="hibernate.connection.driver_class">com.mysql.jdbc.Driver</property>
    <property name="hibernate.connection.url">jdbc:mysql://localhost:5029/db_shard00</property>
    <property name="hibernate.connection.username">root</property>
    <property name="connection.password"></property>
    <property name="connection.pool_size">1</property>
    <property name="hibernate.dialect">org.hibernate.dialect.MySQLDialect</property>
    <property name="hibernate.connection.shard_id">0</property>
    <property name="hibernate.shard.enable_cross_shard_relationship_checks">true</property>
  </session-factory>
</hibernate-configuration>

Each file contains configuration of a single shard. To add more shards to the server, the simply create additional XML files in sequence in the configuration directory and assign a unique shard_id to each shard. Connection details in the configuration file determine whether the shards are hosted on a single or multiple machines. The configuration is detected and loaded when the server is started.

To build the module, run:

mvn clean install

To verify that your shards are configured correctly, use the integration testsuite:

mvn install -P integration-tests

To determine whether your setup meets your perfomance requirements, feel free to use the sample queries provided in our performance testsuite:

mvn install -P perf-tests
