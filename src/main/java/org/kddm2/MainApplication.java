package org.kddm2;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.kddm2.indexing.IndexStatsHelper;
import org.kddm2.lucene.IndexingUtils;
import org.kddm2.search.entity.*;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;

@SpringBootApplication
@EnableWebMvc
@EnableAsync
public class MainApplication extends WebMvcConfigurerAdapter{
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**").addResourceLocations("/static/");
    }

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
    public Set<String> vocabulary(@Value("${vocabulary_file}") Resource vocabularyFile) {
        //TODO: load on startup
        //TODO: case sensitive page titles
        try {

            InputStream inputStream = vocabularyFile.getInputStream();
            if (inputStream == null) {
                throw new BeanCreationException("Could not find vocabulary resource in classpath");
            }
            return IndexingUtils.readDictionary(inputStream);
        } catch (IOException e) {
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
            throw new BeanCreationException("Error creating entity linker", e);
        }
    }

    @Bean
    public EntityIdentifier entityIdentifier(Map<String, EntityWeightingAlgorithm> algorithms, EntityTools tools) {
        return new EntityIdentifier(algorithms, tools, Settings.ENTITY_CUTOFF_RATE_AFTER_IDENTIFICATION);
    }


    @Bean
    public Map<String, EntityWeightingAlgorithm> entityWeightingAlgorithms(IndexStatsHelper indexStatsHelper, EntityTools entityTools) {
        Map<String, EntityWeightingAlgorithm> algorithms = new HashMap<>();
        algorithms.put(EntityWeightingAlgorithm.KEYPHRASENESS_ID, new EntityWeightingKeyphraseness(indexStatsHelper, entityTools));
        algorithms.put(EntityWeightingAlgorithm.TF_IDF_ID, new EntityWeightingTFIDF(indexStatsHelper, entityTools));
        return algorithms;
    }
}
