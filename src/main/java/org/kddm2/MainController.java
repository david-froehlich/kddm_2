package org.kddm2;


import org.kddm2.indexing.IndexingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainController {

    private IndexingService indexingService;

    @Autowired
    public MainController(IndexingService indexingService) {
        this.indexingService = indexingService;
    }

    @RequestMapping("/")
    public String index() {
        return "Hello world";
    }

    @RequestMapping("/indexing/start")
    public String startIndexing() {
        if (indexingService.isRunning())
            return "Indexing is already running";

        indexingService.start();
        return "Started indexing process";
    }
}
