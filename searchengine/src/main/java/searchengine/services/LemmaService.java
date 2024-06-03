package searchengine.services;

import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.Site;

public interface LemmaService {
    Lemma getLemma(String lemmaText, Site site);

}
