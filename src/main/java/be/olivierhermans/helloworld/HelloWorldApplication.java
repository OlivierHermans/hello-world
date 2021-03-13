package be.olivierhermans.helloworld;

import be.olivierhermans.helloworld.service.CustomStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.*;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.vendor.HibernateJpaDialect;
import org.springframework.retry.RetryPolicy;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@EnableBatchProcessing
@SpringBootApplication
@RequiredArgsConstructor
public class HelloWorldApplication {

    private final CustomStatusService service;
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public ItemReader<String> itemReader() {
        AtomicInteger counter = new AtomicInteger(0);
        return () -> {
            System.out.println("In ItemReader::read");
            int value = counter.getAndIncrement();
            return value == 0 ? "now: " + LocalDateTime.now().toString() : null;
        };
    }

    @Bean
    public ItemWriter<String> itemWriter() {
        return (List<? extends String> list) -> {
            System.out.println(String.format("In ItemWriter::write with %d items", list.size()));
            list.stream().forEach(service::createCustomStatus);
            throw new IllegalStateException("Test exception from item writer");
        };
    }

    @Bean
    public Step taskletStep() {
        return stepBuilderFactory
                .get("step1")
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("Hello, World!");
                    service.createCustomStatus("now: " + LocalDateTime.now().toString());
                    return RepeatStatus.FINISHED;
//                    throw new RuntimeException("Test exception from tasklet");
                })
                .allowStartIfComplete(true)
                .build();
    }

    @Bean
    public Step chunkStep() {
        return stepBuilderFactory
                .get("step1")
                .<String, String>chunk(1)
                .reader(itemReader())
                .writer(itemWriter())
                .faultTolerant()
                .skip(IllegalStateException.class)
                .noRollback(IllegalStateException.class)
                .noRetry(IllegalStateException.class)
                .allowStartIfComplete(true)
                .build();
    }

    @Bean
    public Job job() {
        return jobBuilderFactory.get("job").start(chunkStep()).build();
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
