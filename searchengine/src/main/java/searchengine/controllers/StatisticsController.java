package searchengine.controllers;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.Responses.FalseResponse;
import searchengine.Responses.IndexResponse;
import searchengine.Responses.SearchResponse;
import searchengine.config.ConfiguredSearch;
import searchengine.dto.searchDto.SearchDto;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.IndexService;
import searchengine.services.SearchService;
import searchengine.services.StatisticsService;
import java.io.IOException;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api")
public class StatisticsController {

    private final StatisticsService statisticsService;
    private final IndexService indexService;
    private final SearchService searchService;

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<IndexResponse> startIndexing() throws InterruptedException {
        IndexResponse site = indexService.startIndexing();
        return ResponseEntity.ok(site);
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<IndexResponse> stopIndexing() throws InterruptedException {
        IndexResponse site = indexService.stopIndexing();
        return ResponseEntity.ok(site);
    }

    @PostMapping("/indexPage")
    public ResponseEntity<IndexResponse> indexPage(@RequestParam("url") String url) throws IOException {
        IndexResponse indexPage = indexService.indexPage(url);
        return ResponseEntity.ok(indexPage);

    }


    @GetMapping("/search")
    public ResponseEntity<?> search(ConfiguredSearch configuredSearch) throws IOException {
        List<SearchDto> searchResponse = configuredSearch.getSite() == null ?
                searchService.searchByAllSites(configuredSearch) : searchService.searchBySite(configuredSearch);
        return ResponseEntity.ok(searchResponse.isEmpty() ?
                new FalseResponse(false, "Notfound") :
                new SearchResponse(true, searchResponse.size(),
                        searchResponse));
    }

}
