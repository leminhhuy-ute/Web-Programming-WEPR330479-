package topicmanagement.config;

import java.util.Properties;
import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.*;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories("topicmanagement.repository")
public class JpaConfig {
  @Bean
  DataSource dataSource() {
    DatabaseConfigLoader.DatabaseSettings config = DatabaseConfigLoader.load();
    DriverManagerDataSource ds = new DriverManagerDataSource();
    ds.setDriverClassName("com.mysql.cj.jdbc.Driver");
    ds.setUrl(config.url());
    ds.setUsername(config.username());
    ds.setPassword(config.password());
    return ds;
  }

  @Bean
  LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource ds) {
    LocalContainerEntityManagerFactoryBean f = new LocalContainerEntityManagerFactoryBean();
    f.setDataSource(ds);
    f.setPackagesToScan("topicmanagement.entity");
    f.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
    Properties p = new Properties();
    p.put("hibernate.hbm2ddl.auto", "validate");
    p.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
    p.put("hibernate.jdbc.time_zone", "Asia/Ho_Chi_Minh");
    f.setJpaProperties(p);
    return f;
  }

  @Bean
  PlatformTransactionManager transactionManager(jakarta.persistence.EntityManagerFactory emf) {
    return new JpaTransactionManager(emf);
  }
}
