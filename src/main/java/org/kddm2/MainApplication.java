package org.kddm2;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.kddm2.indexing.IndexStatsHelper;
import org.kddm2.lucene.IndexingUtils;
import org.kddm2.search.entity.*;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.concurrent.Executor;

@SpringBootApplication
@EnableWebMvc
@EnableAsync
public class MainApplication {

    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class, args);
    }

    @Bean
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("IndexingService-");
        executor.initialize();
        return executor;
    }

    @Bean
    public Path indexPath() {
        return Paths.get("/tmp/wikificationLuceneIndex");
    }

    @Bean
    public Directory indexDirectory() {
        try {
            return FSDirectory.open(indexPath());
        } catch (IOException e) {
            throw new BeanCreationException("Error creating Lucene directory", e);
        }
    }

    //TODO: maybe put vocabulary in separate type to avoid confusion?
    @Bean
    public Set<String> vocabulary() {
        try {
            URL resource = getClass().getClassLoader().getResource(Settings.VOCABULARY_PATH);
            if (resource == null) {
                throw new BeanCreationException("Could not find vocabulary resource in classpath");
            }
            return IndexingUtils.readDictionary(resource.toURI());
        } catch (URISyntaxException e) {
            throw new BeanCreationException("Error reading vocabulary file", e);
        }
    }

    @Bean
    public EntityTools entityTools(Set<String> vocabulary) {
        return new EntityTools(vocabulary);
    }

    @Bean
    public EntityLinker entityLinker(Directory indexDirectory) {
        try {
            return new EntityLinker(indexDirectory);
        } catch (IOException e) {
            throw new BeanCreationException("Error creating entity linker");
        }
    }

    @Bean
    public EntityIdentifier entityIdentifier(EntityWeightingAlgorithm algorithm, EntityTools tools) {
        return new EntityIdentifier(algorithm, tools, Settings.ENTITY_CUTOFF_RATE);
    }


    @Bean
    public EntityWeightingAlgorithm entityWeightingAlgorithm(IndexStatsHelper indexStatsHelper, EntityTools entityTools) {
        return new EntityWeightingTFIDF(indexStatsHelper, entityTools);
    }
}
