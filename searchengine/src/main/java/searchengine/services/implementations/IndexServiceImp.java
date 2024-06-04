package searchengine.services.implementations;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.HttpStatusException;
import org.jsoup.nodes.Document;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import searchengine.Responses.IndexResponse;
import searchengine.config.SitesList;
import searchengine.lemmatization.Lemmatizater;
import searchengine.model.*;
import searchengine.config.SiteFromConf;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;
import searchengine.services.ConnecterService;
import searchengine.services.IndexService;
import searchengine.crawler.WebCrawler;
import searchengine.crawler.WebCrawlerExecutor;
import searchengine.services.LemmaService;
import searchengine.util.LinkStructure;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Service
@RequiredArgsConstructor
@Slf4j
public class IndexServiceImp implements IndexService {

    private final LemmaRepository lemmaRepository;
    public static volatile boolean stopIndexing = false;
    private final SitesList sitesList;
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaService lemmaService;
    private final IndexRepository indexRepository;
    private final ConnecterService connecterService;

    @Override
    public IndexResponse startIndexing() {
        lemmaRepository.deleteAll();
        pageRepository.deleteAll();
        siteRepository.deleteAll();
        indexRepository.deleteAll();
        WebCrawler.stop = false;
        if (isIndexing()) {
            return new IndexResponse(false, "Индексация уже запущена");
        }
        for (SiteFromConf siteFromConf1 : sitesList.getSites()) {
            Site site = new Site();
            Page page = new Page();
            Lemma lemma = new Lemma();
            IndexTable indexTable = new IndexTable();
            site.setName(siteFromConf1.getName());
            site.setUrl(siteFromConf1.getUrl().replace("www.", ""));
            LocalDateTime statusTime = LocalDateTime.now();
            site.setStatusTime(statusTime);
            site.setStatus(Status.INDEXING);
            page.setSite(site);
            WebCrawlerExecutor webCrawlerExecutor = new WebCrawlerExecutor(connecterService, page, pageRepository, site, siteRepository, lemmaService, lemmaRepository, indexTable, indexRepository, lemma);
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.submit(webCrawlerExecutor);
        }
        return new IndexResponse(true, "");
    }


    @Override
    public IndexResponse stopIndexing() {
        WebCrawler.stop = true;
        stopIndexing = true;
        log.info("SHUTTING FORK");
        List<Site> perSite = siteRepository.findAll();
        for (Site site : perSite) {
            if (site.getStatus().equals(Status.INDEXING)) {
                site.setStatus(Status.FAILED);
                site.setLastError("Индексация остановлена пользователем");
                siteRepository.saveAndFlush(site);
            } else {
                return new IndexResponse(false, "Индексация не запущена");
            }
        }

        return new IndexResponse(true, "");
    }

    @Override
    public IndexResponse indexPage(String link) throws IOException {
        stopIndexing = false;
        List<IndexTable> indexList = new ArrayList<>();
        LinkStructure linkStructure = LinkStructure.getFromLink(link);
        LocalDateTime statusTime = LocalDateTime.now();
        Site site = siteRepository.findSiteByUrl(linkStructure.getUrl());
        site = setSite(site, linkStructure, statusTime);
        Page paths = new Page();
        paths.setPath(Objects.requireNonNullElse(linkStructure.getPath(), ""));
        updatePath(site, paths);
        try {
            Document connection = connecterService.connect(link);
            if (connection.connection().response().statusCode() < HttpStatus.BAD_REQUEST.value()) {
                paths.setCode(connection.connection().response().statusCode());
                paths.setContent(connection.html());
                paths.setSite(site);
                String words = connection.text();
                HashMap<String, Integer> calculatedRussianWords = Lemmatizater.splitTextIntoWords(words);
                siteRepository.save(site);
                pageRepository.save(paths);
                for (Map.Entry<String, Integer> entry : calculatedRussianWords.entrySet()) {
                    if (stopIndexing) {
                        indexRepository.deleteByPageId(paths.getId());
                        lemmaRepository.deleteBySiteId(site.getId());
                        return new IndexResponse(false, "Индексация остановлена пользователем");
                    }
                    setAndInitializeIndexList(entry, paths, site, indexList);
                }
                indexRepository.saveAll(indexList);
                site.setStatus(Status.INDEXED);
                siteRepository.save(site);
                return new IndexResponse(true);
            }
        } catch (HttpStatusException e) {
            log.info("Url Неверный");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return new
                IndexResponse(false, "Данная страница находится за пределами сайтов,\n" +
                "указанных в конфигурационном файле");
    }

    private Site setSite(Site site, LinkStructure linkStructure, LocalDateTime statusTime) {
        if (site == null) {
            site = new Site();
            site.setName(linkStructure.getNameOfSite());
        }
        site.setStatus(Status.INDEXING);
        site.setUrl(linkStructure.getUrl());
        site.setStatusTime(statusTime);
        return site;
    }

    private void setAndInitializeIndexList(Map.Entry<String, Integer> entry, Page paths, Site site, List<IndexTable> indexList) {
        IndexTable indexTable = new IndexTable();
        indexTable.setPage(paths);
        String lemmaText = entry.getKey();
        Lemma existingLemma = lemmaService.getLemma(lemmaText, site);
        indexTable.setLemma(existingLemma);
        indexTable.setRank(entry.getValue());
        indexList.add(indexTable);
    }


    @Override
    public boolean isIndexing() {
        List<Site> site = siteRepository.findAll();
        return site.stream().anyMatch(siteUrlInfo -> siteUrlInfo.getStatus().equals(Status.INDEXING));
    }

    public void updatePath(Site site, Page page) {
        List<Page> existingPage = pageRepository.findPageBySiteId(site.getId());
        existingPage.stream().filter(page1 -> page1 != null && page1.getPath().equals(page.getPath())).forEach(page1 -> {
            List<Integer> lemmaId = indexRepository.findLemmasIdByPageId(page1.getId());
            List<Lemma> lemmaList = lemmaRepository.findByLemmaIds(lemmaId);
            lemmaList.forEach(perLemma -> {
                if (perLemma.getFrequency() > 1) {
                    perLemma.setFrequency(perLemma.getFrequency() - 1);
                    lemmaRepository.save(perLemma);
                } else {
                    lemmaRepository.delete(perLemma);
                }
            });
            pageRepository.deleteById(page1.getId());
            indexRepository.deleteByPageId(page1.getId());
        });
    }


}