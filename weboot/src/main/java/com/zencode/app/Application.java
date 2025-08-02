package com.zencode.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import javax.sql.DataSource;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import java.util.concurrent.Executor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.core.task.AsyncTaskExecutor;

@SpringBootApplication
public class Application implements WebMvcConfigurer{

    private static final Logger logger = LogManager.getLogger(Application.class);

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
        logger.debug("🚀 Application started...");
    }

    @Bean
    public DataSource dataSource() {
        logger.debug("Setting up HikariCP datasource...");
        HikariDataSource ds = new HikariDataSource();
        ds.setDriverClassName("org.postgresql.Driver");
        ds.setJdbcUrl("jdbc:postgresql://localhost:5432/testdb1");
        ds.setUsername("user2");
        ds.setPassword("pass123");
        ds.setMaximumPoolSize(10);
        return ds;
    }

    @Bean
    public DataSourceInitializer dataSourceInitializer(DataSource dataSource) {
        //DataSource dataSource_ = dataSource();
        logger.debug("trying to initialise the database!");
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("sql/schema.sql"));
        populator.addScript(new ClassPathResource("sql/data.sql"));

        DataSourceInitializer initializer = new DataSourceInitializer();
        initializer.setDataSource(dataSource);
        initializer.setDatabasePopulator(populator);
        //initializer.setEnabled(true); // optional, can be based on environment
        logger.debug("DataSourceInitializer configured...");

        return initializer;
    }


   @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        configurer.setTaskExecutor(createAsyncTaskExecutor());
    }

    private AsyncTaskExecutor createAsyncTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);        // Number of threads to keep in the pool, even if idle
        executor.setMaxPoolSize(25);        // Maximum number of threads that can be created
        executor.setQueueCapacity(25);      // Capacity of the queue for tasks waiting to be executed
        executor.setThreadNamePrefix("MyAsyncTask-"); // A prefix for the thread names
        executor.initialize();
        return executor;
    }
}

