package searchengine.services;

import org.jsoup.nodes.Document;
import java.io.IOException;


public interface ConnecterService {

     Document connect(String perLink) throws IOException, InterruptedException;

}