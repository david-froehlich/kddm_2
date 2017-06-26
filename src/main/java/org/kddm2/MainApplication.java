package org.kddm2;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.kddm2.indexing.IndexStatsHelper;
import org.kddm2.indexing.IndexingService;
import org.kddm2.indexing.IndexingVocabulary;
import org.kddm2.search.entity.*;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

@SpringBootApplication
@EnableWebMvc
@EnableAsync
public class MainApplication extends WebMvcConfigurerAdapter {
    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class, args);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**").addResourceLocations("/static/");
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

    @Bean
    public IndexingVocabulary vocabulary(Settings settings) {
        System.out.println(settings.toString());
        return new IndexingVocabulary(settings.getVocabularyFile(), settings.getWikiXmlFile());
    }

    @Bean
    public Path vocabularyPath() {
        return Paths.get("/tmp/vocabulary.txt");
    }

    @Bean
    public EntityTools entityTools(IndexingVocabulary vocabulary, Settings settings) {
        return new EntityTools(vocabulary, settings.getMaxShingleSize());
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
    public EntityIdentifier entityIdentifier(Map<String, EntityWeightingAlgorithm> algorithms, EntityTools tools, Settings settings) {
        return new EntityIdentifier(algorithms, tools, settings.getEntityCutoffRateAfterIdentification());
    }

    @Bean
    public Map<String, EntityWeightingAlgorithm> entityWeightingAlgorithms(IndexStatsHelper indexStatsHelper, EntityTools entityTools) {
        Map<String, EntityWeightingAlgorithm> algorithms = new HashMap<>();
        algorithms.put(EntityWeightingAlgorithm.KEYPHRASENESS_ID, new EntityWeightingKeyphraseness(indexStatsHelper, entityTools));
        algorithms.put(EntityWeightingAlgorithm.TF_IDF_ID, new EntityWeightingTFIDF(indexStatsHelper, entityTools));
        return algorithms;
    }

    @Bean
    public IndexingService indexingService(Directory indexDirectory, IndexingVocabulary vocabulary, Settings settings) {
        return new IndexingService(indexDirectory, vocabulary, settings.getWikiXmlFile(), settings.getIndexingConsumerCount(), settings.getMaxShingleSize());
    }
}
