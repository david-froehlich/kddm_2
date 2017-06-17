package org.kddm2;

import org.kddm2.indexing.IndexingService;
import org.kddm2.indexing.InvalidIndexException;
import org.kddm2.indexing.InvalidWikiFileException;
import org.kddm2.search.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.mvc.EndpointHandlerMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.ArrayList;
import java.util.LinkedList;
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

    @PostMapping(value="/wikify",headers="content-type=text/plain")
    public List<EntityLink> wikify(@RequestBody String text) throws InvalidIndexException {
        List<EntityCandidateWeighted> candidates = entityIdentifier.identifyEntities(text.toLowerCase());
        List<EntityLink> entityLinks = entityLinker.identifyLinksForCandidates(candidates);
        return entityLinks;
    }

    @PostMapping("/wikifyHC")
    public List<EntityLink> wikifyHC() throws InvalidIndexException {
        return getLinksForHardcodedText();
    }

    private List<EntityLink> getLinksForHardcodedText()  {
        String text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Aliquam eu iaculis lectus. " +
                "Nam mi dui, porta at finibus ac, tincidunt vitae enim. Sed efficitur in odio eu cursus. " +
                "Suspendisse rutrum tortor aliquet, vehicula nisi et, auctor lorem. Suspendisse vel euismod orci, " +
                "vitae blandit dui. Donec vel enim quis enim aliquam aliquet. Nullam sagittis tristique magna, laoreet " +
                "convallis sapien. Ut a porttitor turpis, nec mattis diam. Pellentesque tincidunt sapien vel augue aliquam lobortis. " +
                "Cras ex dui, volutpat at est non, viverra mollis justo. Nullam facilisis semper nulla, et luctus diam rutrum vel. " +
                "Quisque a varius dui, non venenatis quam. Nullam eu tellus quis ante venenatis pulvinar ac non libero. " +
                "Pellentesque tempus ultricies efficitur. Duis tempus faucibus eleifend. Integer molestie euismod libero id egestas.";

        String[] entities = new String[]{"dolor sit amet", "efficitur", "aliquam lobortis", "non venenatis"};
        List<EntityDocument> dummyTargets = new LinkedList<>();
        dummyTargets.add(new EntityDocument("lorem_ipsum", -1, 1));
        dummyTargets.add(new EntityDocument("bobby_robson", -1, 1));

        List<EntityLink> links = new LinkedList<>();
        for (String s :
                entities) {
            int start = text.indexOf(s);
            int end = start + s.length() - 1;
            links.add(new EntityLink(new EntityCandidate(start, end, text), dummyTargets));
        }

        return links;
    }

}
