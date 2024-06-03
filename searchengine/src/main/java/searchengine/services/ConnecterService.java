package searchengine.services;

import lombok.Data;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.concurrent.ConcurrentSkipListSet;

import static java.lang.Thread.sleep;



public interface ConnecterService {

     Document connect(String perLink) throws IOException, InterruptedException;

}