package searchengine.crawler;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import searchengine.config.NetworkSettings;
import searchengine.lemmatization.Lemmatizater;
import searchengine.model.*;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.*;
import static java.lang.Thread.sleep;

@AllArgsConstructor
@Getter
public class WebCrawler extends RecursiveAction {

    private final CopyOnWriteArrayList<Page> pages = new CopyOnWriteArrayList<>();
    private final NetworkSettings networkSettings;
    private Page path;
    public static volatile boolean stop = false;
    private final PageRepository pageRepository;
    private Site site;
    private final SiteRepository siteRepository;
    private final LemmaRepository lemmaRepository;
    private IndexTable indexTable;
    private IndexRepository indexRepository;
    private Lemma lemma;
    private final static CopyOnWriteArrayList<String> linksVisited = new CopyOnWriteArrayList<>();

    @Override
    protected void compute() {
        List<IndexTable> indexList = new ArrayList<>();
        linksVisited.add(site.getUrl());
        siteRepository.save(site);
        ConcurrentSkipListSet<String> links = getUrls(site.getUrl());
        for (String perLink : links) {
            if (!linksVisited.contains(perLink)) {
                path = new Page();
                linksVisited.add(perLink);
                path.setPath(perLink.replace(site.getUrl(), ""));
                addChildren(path);
                path.setSite(site);
                try {
                    if (stop) {
                        return;
                    }
                    sleep(5000);
                    Result result = connect(perLink);
                    path.setCode(result.connection().statusCode());
                    String content = result.document().html();
                    String wordsPerPath = result.document().text();
                    path.setContent(content);
                    pageRepository.save(path);
                    indexTable.setPage(path);
                    HashMap<String, Integer> calculatedRussianWords = Lemmatizater.splitTextIntoWords(wordsPerPath);
                    for (Map.Entry<String, Integer> entry : calculatedRussianWords.entrySet()) {
                        IndexTable indexTable = new IndexTable();
                        indexTable.setPage(path);
                        String lemma = entry.getKey();
                        Lemma existingLemma = lemmaRepository.findByLemma(lemma);
                        if (existingLemma == null) {
                            existingLemma = new Lemma();
                            existingLemma.setSite(site);
                            existingLemma.setLemma(lemma);
                            existingLemma.setFrequency(1);
                            lemmaRepository.save(existingLemma);
                        } else {
                            existingLemma.setFrequency(existingLemma.getFrequency() + 1);
                            lemmaRepository.save(existingLemma);
                        }
                        indexTable.setLemma(existingLemma);
                        indexTable.setRank(entry.getValue());
                        indexList.add(indexTable);
                    }
                    indexRepository.saveAll(indexList);
                } catch (InterruptedException | IOException e) {
                    site.setLastError("Ошибка индексации: главная\n" +
                            "страница сайта недоступна");
                    siteRepository.save(site);
                }
            }
        }
        indexRepository.save(indexTable);
        site.setStatus(Status.INDEXED);
        CopyOnWriteArrayList<WebCrawler> mapOfSiteTasks = new CopyOnWriteArrayList<>();
        for (Page page : getPages()) {
            WebCrawler webCrawlerTask = new WebCrawler(networkSettings, page, pageRepository, site, siteRepository, lemmaRepository, indexTable, indexRepository, lemma);
            mapOfSiteTasks.add(webCrawlerTask);
            webCrawlerTask.fork();
        }
        mapOfSiteTasks.forEach(ForkJoinTask::join);
    }

    private Result connect(String perLink) throws IOException, InterruptedException {
        sleep(5000);
        Connection.Response connection = Jsoup.connect(perLink)
                .ignoreContentType(true)
                .userAgent(networkSettings.getUserAgents().get(new Random().nextInt(6)).toString())
                .referrer(networkSettings.getReferrer())
                .timeout(networkSettings.getTimeout())
                .followRedirects(false)
                .execute();
        Document document = connection.parse();
        return new Result(connection, document);
    }

    private record Result(Connection.Response connection, Document document) {
    }

    public void addChildren(Page page) {
        pages.add(page);
    }


    public ConcurrentSkipListSet<String> getUrls(String url) {
        ConcurrentSkipListSet<String> urls = new ConcurrentSkipListSet<>();
        try {
            sleep(150);
            Result result = connect(url);
            Elements elements = result.document.select("body").select("a");
            for (Element perElement : elements) {
                String link = perElement.absUrl("href");
                if (isLink(link) && !isFile(link)) {
                    urls.add(link);
                }
            }
        } catch (InterruptedException | IOException e) {
            System.out.println(e + "  " + url);
        }
        return urls;
    }

    private boolean isLink(String link) {
        return link.startsWith(site.getUrl());
    }

    private boolean isFile(String link) {
        return link.contains(".pdf")
                || link.contains(".png")
                || link.contains(".gif")
                || link.contains(".eps")
                || link.contains(".webp")
                || link.contains(".doc")
                || link.contains(".pptx")
                || link.contains("?_ga")
                || link.contains(".xlsx")
                || link.contains(".docx")
                || link.contains(".jpg")
                || link.contains("#");
    }

}
