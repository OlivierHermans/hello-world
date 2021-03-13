package be.olivierhermans.helloworld;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@EnableBatchProcessing
@SpringBootApplication
public class HelloWorldApplication {

    @Bean
    public Step step(StepBuilderFactory stepBuilderFactory) {
        return stepBuilderFactory
                .get("step1")
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("Hello, World!");
                    return RepeatStatus.FINISHED;
                })
                .allowStartIfComplete(true)
                .build();
    }

    @Bean
    public Job job(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory) {
        return jobBuilderFactory.get("job").start(step(stepBuilderFactory)).build();
    }

    public static void main(String[] args) {
        SpringApplication.run(HelloWorldApplication.class, args);
    }

}
