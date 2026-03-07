package com.computershop.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * Database configuration for multi-database support.
 * Supports both single database mode and distributed mode (2 databases).
 *
 * Database distribution by module:
 * - DB1 (primary): Users, Products, Categories, Images, Roles
 * - DB2 (orders): Orders, OrderDetails, Carts, CartItems, PaymentTransactions
 *
 * Usage:
 * - Single mode: Default profile, uses single SQL Server
 * - Distributed mode: Set spring.profiles.active=distributed
 */
@Configuration
@EnableTransactionManagement
public class DatabaseConfig {

    // ==================== Single Database Configuration ====================

    @Value("${spring.datasource.url:jdbc:sqlserver://localhost:1433;databaseName=computershop;encrypt=true;trustServerCertificate=true}")
    private String singleDbUrl;

    @Value("${spring.datasource.username:hai}")
    private String singleDbUsername;

    @Value("${spring.datasource.password:hai}")
    private String singleDbPassword;

    @Value("${spring.datasource.driver-class-name:com.microsoft.sqlserver.jdbc.SQLServerDriver}")
    private String driverClassName;

    // ==================== Distributed Database Configuration ====================

    @Value("${spring.datasource.primary.url:jdbc:sqlserver://localhost:1433;databaseName=computershop_main;encrypt=true;trustServerCertificate=true}")
    private String primaryDbUrl;

    @Value("${spring.datasource.primary.username:hai}")
    private String primaryDbUsername;

    @Value("${spring.datasource.primary.password:hai}")
    private String primaryDbPassword;

    @Value("${spring.datasource.orders.url:jdbc:sqlserver://localhost:1434;databaseName=computershop_orders;encrypt=true;trustServerCertificate=true}")
    private String ordersDbUrl;

    @Value("${spring.datasource.orders.username:hai}")
    private String ordersDbUsername;

    @Value("${spring.datasource.orders.password:hai}")
    private String ordersDbPassword;

    // ==================== JPA Properties ====================

    @Value("${spring.jpa.hibernate.ddl-auto:update}")
    private String hibernateDdlAuto;

    @Value("${spring.jpa.show-sql:false}")
    private boolean showSql;

    @Value("${spring.jpa.properties.hibernate.format_sql:true}")
    private boolean formatSql;

    @Value("${spring.jpa.properties.hibernate.dialect:org.hibernate.dialect.SQLServerDialect}")
    private String hibernateDialect;

    // ==================== DataSource Beans ====================

    /**
     * Creates the primary data source for single mode or DB1 in distributed mode.
     */
    @Bean
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource primaryDataSource() {
        boolean isDistributed = isDistributedMode();
        String url = isDistributed ? primaryDbUrl : singleDbUrl;
        String username = isDistributed ? primaryDbUsername : singleDbUsername;
        String password = isDistributed ? primaryDbPassword : singleDbPassword;

        return DataSourceBuilder.create()
                .url(url)
                .username(username)
                .password(password)
                .driverClassName(driverClassName)
                .build();
    }

    /**
     * Creates the orders data source for distributed mode.
     * Only used when distributed mode is active.
     */
    @Bean(name = "ordersDataSource")
    public DataSource ordersDataSource() {
        return DataSourceBuilder.create()
                .url(ordersDbUrl)
                .username(ordersDbUsername)
                .password(ordersDbPassword)
                .driverClassName(driverClassName)
                .build();
    }

    // ==================== EntityManagerFactory Beans ====================

    /**
     * Creates the primary entity manager factory.
     * Manages entities for primary database (Users, Products, Categories, Images, Roles).
     */
    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean primaryEntityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(primaryDataSource());
        em.setPackagesToScan("com.computershop.main.entities"); // Will be updated after entity migration

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setGenerateDdl(true);
        vendorAdapter.setShowSql(showSql);
        em.setJpaVendorAdapter(vendorAdapter);

        em.setJpaProperties(hibernateProperties());

        return em;
    }

    /**
     * Creates the orders entity manager factory.
     * Manages entities for orders database (Orders, OrderDetails, Carts, CartItems).
     */
    @Bean(name = "ordersEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean ordersEntityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(ordersDataSource());
        em.setPackagesToScan("com.computershop.main.entities");

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setGenerateDdl(true);
        vendorAdapter.setShowSql(showSql);
        em.setJpaVendorAdapter(vendorAdapter);

        em.setJpaProperties(hibernateProperties());

        return em;
    }

    // ==================== TransactionManager Beans ====================

    /**
     * Creates the primary transaction manager.
     */
    @Bean
    @Primary
    public PlatformTransactionManager primaryTransactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(primaryEntityManagerFactory().getObject());
        return transactionManager;
    }

    /**
     * Creates the orders transaction manager.
     */
    @Bean(name = "ordersTransactionManager")
    public PlatformTransactionManager ordersTransactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(ordersEntityManagerFactory().getObject());
        return transactionManager;
    }

    // ==================== Helper Methods ====================

    /**
     * Checks if the application is running in distributed mode.
     *
     * @return true if distributed mode is active
     */
    private boolean isDistributedMode() {
        // Check if distributed profile is active
        // This can be configured via spring.profiles.active=distributed
        // For now, we check if the orders datasource URL is different from single DB
        return !singleDbUrl.contains("computershop_main");
    }

    /**
     * Creates Hibernate properties.
     *
     * @return Hibernate properties
     */
    private Properties hibernateProperties() {
        Properties properties = new Properties();
        properties.setProperty("hibernate.hbm2ddl.auto", hibernateDdlAuto);
        properties.setProperty("hibernate.show_sql", String.valueOf(showSql));
        properties.setProperty("hibernate.format_sql", String.valueOf(formatSql));
        properties.setProperty("hibernate.dialect", hibernateDialect);
        return properties;
    }
}
