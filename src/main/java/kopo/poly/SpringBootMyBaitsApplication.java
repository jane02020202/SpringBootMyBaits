package kopo.poly;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class SpringBootMyBaitsApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootMyBaitsApplication.class, args);
    }

}