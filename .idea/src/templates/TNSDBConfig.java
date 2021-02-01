package templates;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import ru.megafon.pom.ConfigSettings;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "ru.megafon.repository.pom",
        entityManagerFactoryRef = "secEntityManagerFactory", transactionManagerRef = "secTransactionManager")
public class TNSDBConfig {
    @Primary
    @Bean
    @ConfigurationProperties(prefix = "pom.datasource")
    public DataSourceProperties secDataSourceProperties() {
        System.setProperty(
                "oracle.net.tns_admin",
                ConfigSettings.path);
        return new DataSourceProperties();
    }

    @Primary
    @Bean(name = "secDataSource")
    public DataSource secDataSource() {
        return secDataSourceProperties()
                .initializeDataSourceBuilder()
                .build();
    }

    @Primary
    @Bean(name = "secEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean primaryEntityManagerFactory(EntityManagerFactoryBuilder builder, @Qualifier("secDataSource") DataSource dataSource) {
        return builder.dataSource(dataSource)
                .packages("ru.megafon.entity.pom")
                .build();
    }

    @Primary
    @Bean(name = "secTransactionManager")
    public PlatformTransactionManager secTransactionManager(
            final @Qualifier("secEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

}
