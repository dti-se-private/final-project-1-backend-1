package org.dti.se.finalproject1backend1.outers.configurations.datastores;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;


@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "org.dti.se.finalproject1backend1.outers.repositories.ones",
        entityManagerFactoryRef = "oneEntityManagerFactory",
        transactionManagerRef = "oneTransactionManager"
)
public class OneDatastoreConfiguration {

    @Autowired
    private Environment environment;

    @Bean
    public DataSource oneDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        String url = String.format(
                "jdbc:postgresql://%s:%s/%s",
                environment.getProperty("datastore.one.host"),
                environment.getProperty("datastore.one.port"),
                environment.getProperty("datastore.one.database")
        );
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl(url);
        dataSource.setUsername(environment.getProperty("datastore.one.user"));
        dataSource.setPassword(environment.getProperty("datastore.one.password"));
        return dataSource;
    }

    @Bean
    public JdbcTemplate oneTemplate(@Qualifier("oneDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean oneEntityManagerFactory(@Qualifier("oneDataSource") DataSource dataSource) {
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setJpaVendorAdapter(vendorAdapter);
        factory.setDataSource(dataSource);
        factory.setPackagesToScan("org.dti.se.finalproject1backend1.inners.models.entities");
        HashMap<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        factory.setJpaPropertyMap(properties);
        return factory;
    }

    @Bean
    public PlatformTransactionManager oneTransactionManager(@Qualifier("oneEntityManagerFactory") LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory.getObject());
        return transactionManager;
    }

}
