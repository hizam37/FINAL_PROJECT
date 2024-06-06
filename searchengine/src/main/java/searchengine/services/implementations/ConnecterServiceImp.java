package searchengine.services.implementations;

import lombok.RequiredArgsConstructor;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import searchengine.config.NetworkSettings;
import searchengine.services.ConnecterService;
import java.io.IOException;
import java.util.Random;
import static java.lang.Thread.sleep;

@Service
@RequiredArgsConstructor
public class ConnecterServiceImp implements ConnecterService {
    private final NetworkSettings networkSettings;

    @Override
    public Document connect(String perLink) throws IOException, InterruptedException {
        sleep(300);
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
