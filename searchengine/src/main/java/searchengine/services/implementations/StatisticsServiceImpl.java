package searchengine.services.implementations;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dto.statistics.DetailedStatisticsItem;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;
import searchengine.model.Site;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;
import searchengine.services.StatisticsService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final SiteRepository siteRepository;

    @Override
    public StatisticsResponse getStatistics() {
        int numberOfSites = (int) siteRepository.countSite();
        TotalStatistics total = new TotalStatistics();
        total.setSites(numberOfSites);
        total.setIndexing(true);
        List<DetailedStatisticsItem> detailedStatisticsItems = new ArrayList<>();
        List<Site> site = siteRepository.findAll();
        setDetailedStatistics(numberOfSites, site, detailedStatisticsItems);
        long totalPages = pageRepository.countByPage();
        total.setPages((int) totalPages);
        long totalLemmas = lemmaRepository.countByLemma();
        total.setLemmas((int) totalLemmas);
        StatisticsResponse response = new StatisticsResponse();
        StatisticsData data = new StatisticsData();
        data.setTotal(total);
        data.setDetailed(detailedStatisticsItems);
        response.setStatistics(data);
        response.setResult(true);
        return response;
    }

    private void setDetailedStatistics(int numberOfSites, List<Site> site, List<DetailedStatisticsItem> detailedStatisticsItems) {
        for (int i = 0; i < numberOfSites; i++) {
            DetailedStatisticsItem item = new DetailedStatisticsItem();
            item.setName(site.get(i).getName());
            int pages = (int) pageRepository.countPageBySiteId(site.get(i).getId());
            item.setPages(pages);
            item.setUrl(site.get(i).getUrl());
            int lemmas = (int) lemmaRepository.countLemmaBySiteId(site.get(i).getId());
            item.setLemmas(lemmas);
            String status = siteRepository.findStatusByUrl(site.get(i).getUrl());
            LocalDateTime statusTime = siteRepository.findStatusTimeByUrl(site.get(i).getUrl());
            String lastError = siteRepository.findLastErrorByUrl(site.get(i).getUrl());
            item.setStatus(status);
            item.setStatusTime(statusTime);
            item.setError(Objects.requireNonNullElse(lastError, ""));
            detailedStatisticsItems.add(item);
        }
    }

}

