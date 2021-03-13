package be.olivierhermans.helloworld;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.*;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.vendor.HibernateJpaDialect;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@EnableBatchProcessing
@SpringBootApplication
public class HelloWorldApplication {

    @Bean
    public Step step(StepBuilderFactory stepBuilderFactory) {
        return stepBuilderFactory
                .get("step1")
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("Hello, World!");
                    System.exit(0);
                    return RepeatStatus.FINISHED;
                })
                .allowStartIfComplete(true)
                .build();
    }

    @Bean
    public Job job(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory) {
        return jobBuilderFactory.get("job").start(step(stepBuilderFactory)).build();
    }

    @Bean
    public BatchConfigurer batchConfigurer(DataSource dataSource, EntityManagerFactory entityManagerFactory) {
        return new DefaultBatchConfigurer() {
            @Override
            public PlatformTransactionManager getTransactionManager() {
                JpaTransactionManager transactionManager = new JpaTransactionManager();
                transactionManager.setDataSource(dataSource);
                transactionManager.setEntityManagerFactory(entityManagerFactory);
                transactionManager.setJpaDialect(new HibernateJpaDialect());
                return transactionManager;
            }
        };
    }

    public static void main(String[] args) {
        SpringApplication.run(HelloWorldApplication.class, args);
    }

}
