package searchengine.services.implementations;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import searchengine.Responses.IndexResponse;
import searchengine.config.NetworkSettings;
import searchengine.config.SitesList;
import searchengine.lemmatization.Lemmatizater;
import searchengine.model.*;
import searchengine.config.SiteFromConf;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;
import searchengine.services.IndexService;
import searchengine.crawler.WebCrawler;
import searchengine.crawler.WebCrawlerExecutor;
import searchengine.util.LinkStructure;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class IndexServiceImp implements IndexService {

    private final LemmaRepository lemmaRepository;
    public static volatile boolean stopIndexing = false;
    private final NetworkSettings networkSettings;
    private final SitesList sitesList;
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final IndexRepository indexRepository;

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
            WebCrawlerExecutor webCrawlerExecutor = new WebCrawlerExecutor(networkSettings, page, pageRepository, site, siteRepository, lemmaRepository, indexTable, indexRepository, lemma);
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
        Page paths;
        LocalDateTime statusTime = LocalDateTime.now();
        Site site = siteRepository.findSiteByUrl(linkStructure.getUrl());
        if (site == null) {
            site = new Site();
            site.setName(linkStructure.getNameOfSite());
        }
        site.setStatus(Status.INDEXING);
        Lemma existingLemma;
        site.setUrl(linkStructure.getUrl());
        paths = new Page();
        paths.setPath(Objects.requireNonNullElse(linkStructure.getPath(), ""));
        site.setStatusTime(statusTime);
        updatePath(site, paths);
        try {
            Connection.Response connection = Jsoup.connect(link).ignoreContentType(true)
                    .userAgent(networkSettings.getUserAgents().get(new Random().nextInt(7)).toString())
                    .referrer(networkSettings.getReferrer())
                    .timeout(networkSettings.getTimeout())
                    .followRedirects(false)
                    .execute();
            Document document = connection.parse();
            if (document.connection().response().statusCode() < HttpStatus.BAD_REQUEST.value()) {
                paths.setCode(document.connection().response().statusCode());
                String content = document.html();
                paths.setContent(content);
                paths.setSite(site);
                String words = document.text();
                HashMap<String, Integer> calculatedRussianWords = Lemmatizater.splitTextIntoWords(words);
                siteRepository.save(site);
                pageRepository.save(paths);
                for (Map.Entry<String, Integer> entry : calculatedRussianWords.entrySet()) {
                    if (stopIndexing) {
                        indexRepository.deleteByPageId(paths.getId());
                        lemmaRepository.deleteBySiteId(site.getId());
                        return new IndexResponse(false, "Индексация остановлена пользователем");
                    }
                    IndexTable indexTable = new IndexTable();
                    indexTable.setPage(paths);
                    String lemmaText = entry.getKey();
                    existingLemma = lemmaRepository.findLemmaBySiteId(lemmaText, site.getId());
                    if (existingLemma == null) {
                        existingLemma = new Lemma();
                        existingLemma.setSite(site);
                        existingLemma.setLemma(lemmaText);
                        existingLemma.setFrequency(1);
                        lemmaRepository.save(existingLemma);
                    } else {
                        existingLemma.setSite(paths.getSite());
                        existingLemma.setFrequency(existingLemma.getFrequency() + 1);
                        lemmaRepository.save(existingLemma);
                    }
                    indexTable.setLemma(existingLemma);
                    indexTable.setRank(entry.getValue());
                    indexList.add(indexTable);
                }
                indexRepository.saveAll(indexList);
                site.setStatus(Status.INDEXED);
                siteRepository.save(site);
                return new IndexResponse(true);
            }
        } catch (HttpStatusException e) {
            log.info("Url Неверный");
        }
        return new
                IndexResponse(false, "Данная страница находится за пределами сайтов,\n" +
                "указанных в конфигурационном файле");
    }

    @Override
    public boolean isIndexing() {
        List<Site> site = siteRepository.findAll();
        for (Site siteUrlInfo : site) {
            if (siteUrlInfo.getStatus().equals(Status.INDEXING)) {
                return true;
            }
        }
        return false;
    }

    public void updatePath(Site site, Page page) {
        List<Page> existingPage = pageRepository.findPageBySiteId(site.getId());
        for (Page page1 : existingPage) {
            if (page1 != null && page1.getPath().equals(page.getPath())) {
                List<Integer> lemmaId = indexRepository.findLemmasIdByPageId(page1.getId());
                List<Lemma> lemmaList = lemmaRepository.findByLemmaIds(lemmaId);
                for (Lemma perLemma : lemmaList) {
                    if (perLemma.getFrequency() > 1) {
                        perLemma.setFrequency(perLemma.getFrequency() - 1);
                        lemmaRepository.save(perLemma);
                    } else {
                        lemmaRepository.delete(perLemma);
                    }
                }
                pageRepository.deleteById(page1.getId());
                indexRepository.deleteByPageId(page1.getId());
            }
        }
    }




}