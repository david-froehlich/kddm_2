package org.kddm2;


import org.kddm2.indexing.IndexingService;
import org.kddm2.indexing.InvalidIndexException;
import org.kddm2.indexing.InvalidWikiFileException;
import org.kddm2.search.entity.EntityCandidateWeighted;
import org.kddm2.search.entity.EntityIdentifier;
import org.kddm2.search.entity.EntityLink;
import org.kddm2.search.entity.EntityLinker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.mvc.EndpointHandlerMapping;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class MainController {
    private static final Logger LOG = LoggerFactory.getLogger(MainController.class);


    private final List<RequestMappingHandlerMapping> mappings;
    private final EntityLinker entityLinker;
    private final EntityIdentifier entityIdentifier;
    private IndexingService indexingService;

    @Autowired
    public MainController(IndexingService indexingService, List<RequestMappingHandlerMapping> mappings, EntityLinker entityLinker, EntityIdentifier entityIdentifier) {
        this.indexingService = indexingService;
        this.mappings = mappings;
        this.entityLinker = entityLinker;
        this.entityIdentifier = entityIdentifier;
    }

    /**
     * Provides a list of endpoints for documentation.
     */
    @GetMapping("/")
    public List<RESTEndpoint> index() {
        List<RESTEndpoint> endpoints = new ArrayList<>();
        for (RequestMappingHandlerMapping mapping :
                mappings) {
            if (!(mapping instanceof EndpointHandlerMapping)) {
                Map<RequestMappingInfo, HandlerMethod> handlerMethods = mapping.getHandlerMethods();
                handlerMethods.forEach((requestMappingInfo, handlerMethod) -> {
                            if (!requestMappingInfo.getMethodsCondition().isEmpty()) {
                                endpoints.add(new RESTEndpoint(requestMappingInfo.getPatternsCondition().getPatterns(),
                                        requestMappingInfo.getMethodsCondition().getMethods().stream()
                                                .map(Enum::toString).collect(Collectors.toSet())));
                            }
                        }
                );
            }
        }
        return endpoints;
    }

    @GetMapping("/indexing/start")
    public String startIndexing() throws InvalidWikiFileException {
        if (indexingService.isRunning()) {
            return "Indexing is already running";
        }
        indexingService.start();
        return "Started indexing process";
    }

    @GetMapping("/indexing/")
    public IndexingService.IndexingStatus indexingStatus() {
        return indexingService.getStatus();
    }

    @PostMapping("/wikify")
    public List<EntityLink> wikify(@RequestBody String text) throws InvalidIndexException {
        List<EntityCandidateWeighted> candidates = entityIdentifier.identifyEntities(text);
        return entityLinker.identifyLinksForCandidates(candidates);
    }
}