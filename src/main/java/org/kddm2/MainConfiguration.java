package org.kddm2;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.nio.file.Paths;


@Configuration
public class MainConfiguration {
    @Bean
    public Path indexPath() {
        return Paths.get("/tmp/lucene_dir");
    }
}
