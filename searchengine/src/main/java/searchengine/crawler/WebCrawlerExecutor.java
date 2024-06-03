package searchengine.crawler;

import lombok.extern.slf4j.Slf4j;
import searchengine.model.IndexTable;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;
import searchengine.services.ConnecterService;
import searchengine.services.LemmaService;

import java.util.concurrent.ForkJoinPool;


@Slf4j
public class WebCrawlerExecutor implements Runnable {

    private final ConnecterService connecterService;
    public static ForkJoinPool forkJoinPool;
    private final PageRepository pageRepository;
    private final SiteRepository siteRepository;
    private final LemmaRepository lemmaRepository;
    private final LemmaService lemmaService;
    private final IndexRepository indexRepository;
    private final IndexTable indexTable;
    private final Lemma lemma;
    private final Site site;
    private final Page page;

    public WebCrawlerExecutor(ConnecterService connecterService, Page page, PageRepository pageRepository, Site site, SiteRepository siteRepository,LemmaService lemmaService, LemmaRepository lemmaRepository, IndexTable indexTable, IndexRepository indexRepository, Lemma lemma) {
        this.page = page;
        this.site = site;
        this.lemma=lemma;
        this.pageRepository = pageRepository;
        this.siteRepository = siteRepository;
        this.lemmaService=lemmaService;
        this.lemmaRepository=lemmaRepository;
        this.indexRepository=indexRepository;
        this.indexTable = indexTable;
        this.connecterService =connecterService ;
        forkJoinPool = new ForkJoinPool();
    }

    @Override
    public void run() {
        WebCrawler webCrawler = new WebCrawler(connecterService,page,pageRepository,site, siteRepository,lemmaService,lemmaRepository,indexTable,indexRepository,lemma);
        forkJoinPool.invoke(webCrawler);
    }

}