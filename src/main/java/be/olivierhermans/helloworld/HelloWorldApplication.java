package be.olivierhermans.helloworld;

import be.olivierhermans.helloworld.service.CustomStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.*;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
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
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
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
            log.info("In ItemReader::read");
            int value = counter.getAndIncrement();
            return value == 0 ? "now: " + LocalDateTime.now().toString() : null;
        };
    }

    @Bean
    public ItemWriter<String> itemWriter() {
        return (List<? extends String> list) -> {
            log.info(String.format("In ItemWriter::write with %d items", list.size()));
            list.stream().forEach(service::createCustomStatus);
            throw new IllegalStateException("Test exception from item writer");
        };
    }

    @Bean
    public Step taskletStep() {
        return stepBuilderFactory
                .get("step1")
                .tasklet((contribution, chunkContext) -> {
                    log.info("Hello, World!");
                    service.createCustomStatus("now: " + LocalDateTime.now().toString());
                    return RepeatStatus.FINISHED;
//                    throw new RuntimeException("Test exception from tasklet");
                })
                //.allowStartIfComplete(true) ==> Add an incrementer to the job instead
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
                .skipLimit(Integer.MAX_VALUE)
                .noRollback(IllegalStateException.class)
                .noRetry(IllegalStateException.class)
                //.allowStartIfComplete(true) ==> Add an incrementer to the job instead
                .build();
    }

    private JobExecutionListener jobExecutionListener() {
        return new JobExecutionListener() {
            @Override
            public void beforeJob(JobExecution execution) {
                log.info("Starting job {}", execution.getJobInstance().getJobName());
            }

            @Override
            public void afterJob(JobExecution execution) {
                log.info("Job {} has completed with status {}",
                        execution.getJobInstance().getJobName(),
                        execution.getStatus());
            }
        };
    }

    @Bean
    public Job job() {
        return jobBuilderFactory
                .get("jobWithIncrementer")
                .start(chunkStep())
                .incrementer(new RunIdIncrementer())
                .listener(jobExecutionListener())
                .build();
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
