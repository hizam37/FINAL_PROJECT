package searchengine.services;

import searchengine.Responses.IndexResponse;
import java.io.IOException;

public interface IndexService {

    IndexResponse startIndexing() throws InterruptedException;

    IndexResponse stopIndexing() throws InterruptedException;

    IndexResponse indexPage(String url) throws IOException;

    boolean isIndexing();

}
