package org.dti.se.finalproject1backend1.outers.configurations.datastores;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;


@Configuration
@EnableTransactionManagement
public class OneDatastoreConfiguration {

    @Autowired
    private Environment environment;

    @Bean
    public DataSource oneDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        String url = String.format(
                "jdbc:postgresql://%s:%s@%s:%s/%s",
                environment.getProperty("datastore.one.user"),
                environment.getProperty("datastore.one.password"),
                environment.getProperty("datastore.one.host"),
                environment.getProperty("datastore.one.port"),
                environment.getProperty("datastore.one.database")
        );
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl(url);
        return dataSource;
    }

    @Bean
    public JdbcTemplate oneTemplate(@Qualifier("oneDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean oneEntityManagerFactory(@Qualifier("oneDataSource") DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean entityManagerFactory = new LocalContainerEntityManagerFactoryBean();
        entityManagerFactory.setDataSource(dataSource);
        entityManagerFactory.setPackagesToScan("org.dti.se.finalproject1backend1.inners.models.entities");
        return entityManagerFactory;
    }

    @Bean
    public PlatformTransactionManager oneTransactionManager(@Qualifier("oneEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory);
        return transactionManager;
    }

}
