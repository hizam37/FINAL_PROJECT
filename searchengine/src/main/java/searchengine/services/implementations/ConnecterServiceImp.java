package searchengine.services.implementations;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import searchengine.config.NetworkSettings;
import searchengine.crawler.WebCrawler;
import searchengine.model.Site;
import searchengine.services.ConnecterService;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.ConcurrentSkipListSet;

import static java.lang.Thread.sleep;

@Service
@RequiredArgsConstructor
public class ConnecterServiceImp implements ConnecterService {
    private final NetworkSettings networkSettings;

    @Override
    public Document connect(String perLink) throws IOException, InterruptedException {
            sleep(5000);
            Connection.Response connection = Jsoup.connect(perLink)
                    .ignoreContentType(true)
                    .userAgent(networkSettings.getUserAgents().get(new Random().nextInt(6)).toString())
                    .referrer(networkSettings.getReferrer())
                    .timeout(networkSettings.getTimeout())
                    .followRedirects(false)
                    .execute();
        return connection.parse();

    }

}
