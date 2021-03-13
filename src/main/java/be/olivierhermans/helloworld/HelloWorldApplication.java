package be.olivierhermans.helloworld;

import be.olivierhermans.helloworld.dao.CustomStatusDao;
import be.olivierhermans.helloworld.dao.CustomStatusRepository;
import lombok.RequiredArgsConstructor;
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
import java.time.LocalDateTime;

@EnableBatchProcessing
@SpringBootApplication
@RequiredArgsConstructor
public class HelloWorldApplication {

    private final CustomStatusRepository repository;
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Step step() {
        return stepBuilderFactory
                .get("step1")
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("Hello, World!");
                    repository.save(CustomStatusDao.builder().status("now: " + LocalDateTime.now().toString()).build());
//                    return RepeatStatus.FINISHED;
                    throw new RuntimeException("Test exception");
                })
                .allowStartIfComplete(true)
                .build();
        // set no rollback on a fault tolerant step (chunk based only) to prevent rolling back custom db updates
    }

    @Bean
    public Job job() {
        return jobBuilderFactory.get("job").start(step()).build();
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
