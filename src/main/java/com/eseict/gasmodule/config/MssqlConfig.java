package com.eseict.gasmodule.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.eseict.gasmodule.repository.mssql",
        entityManagerFactoryRef = "mssqlEntityManagerFactory",
        transactionManagerRef = "mssqlTransactionManager"
)
@Profile("prod")
@Slf4j
public class MssqlConfig {

    @Value("${spring.datasource.mssql.url}")
    String jdbcUrl;
    @Value("${spring.datasource.mssql.username}")
    String userName;
    @Value("${spring.datasource.mssql.password}")
    String password;
    @Value("${spring.datasource.mssql.driver-class-name}")
    String driverName;

    @Bean(name = "mssqlDataSource")
    @Primary
    public DataSource mssqlDataSource() {
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName(driverName);
        ds.setUrl(jdbcUrl);
        ds.setUsername(userName);
        ds.setPassword(password);

        log.info("## GAS MSSQL DataSource initialized. URL: {}", jdbcUrl);

        return ds;
    }

    @Bean(name = "mssqlEntityManagerFactory")
    @Primary
    public LocalContainerEntityManagerFactoryBean mssqlEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("mssqlDataSource") DataSource dataSource
    ) {

        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect", "org.hibernate.dialect.SQLServer2012Dialect");
        properties.put("hibernate.hbm2ddl.auto", "none");
        properties.put("hibernate.show_sql", "false");

        return builder
                .dataSource(dataSource)
                .packages("com.eseict.gasmodule.data.mssql")
                .persistenceUnit("mssql")
                .properties(properties)
                .build();
    }

    @Bean(name = "mssqlTransactionManager")
    @Primary
    public PlatformTransactionManager mssqlTransactionManager(
            @Qualifier("mssqlEntityManagerFactory") EntityManagerFactory emf
    ) {
        return new JpaTransactionManager(emf);
    }
}
