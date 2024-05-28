package searchengine.services.implementations;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;
import searchengine.config.ConfiguredSearch;
import searchengine.dto.relevances.AbsoluteRelevanceForAllSites;
import searchengine.dto.relevances.AbsoluteRelevanceForOneSite;
import searchengine.dto.searchDto.SearchDto;
import searchengine.lemmatization.Lemmatizater;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;
import searchengine.services.SearchService;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImp implements SearchService {

    private final IndexRepository indexRepository;
    private final PageRepository pageRepository;
    private final SiteRepository siteRepository;
    private final LemmaRepository lemmaRepository;

    @Override
    public List<SearchDto> searchBySite(ConfiguredSearch configuredSearch) throws IOException {
        List<SearchDto> searchDtoList = new ArrayList<>();
        log.info("LINK USED " + configuredSearch.getSite());
        Site site = siteRepository.findSiteByUrl(configuredSearch.getSite());
        log.info("SITE FOUND " + site.getUrl());
        Integer siteId;
        if (site.getUrl().equals(configuredSearch.getSite())) {
            siteId = siteRepository.getIdByUrl(site.getUrl());
            log.info("FOUND ID " + site.getId());
            double leastPercentage = 50;
            Map<String, Integer> filteredLemmas = Lemmatizater.splitTextIntoWords(configuredSearch.getQuery());
            Map<String, Double> wordsWithFrequencies = new HashMap<>();
            for (Map.Entry<String, Integer> frequenciesWithLemmas : filteredLemmas.entrySet()) {
                log.info("Lemma " + frequenciesWithLemmas.getKey());
                long totalPages = pageRepository.countPageBySiteId(siteId);
                log.info("total pages " + totalPages);
                Double foundFrequencyByLemma = lemmaRepository.findFrequencyByLemmaAndSiteId(frequenciesWithLemmas.getKey(), siteId);
                if (foundFrequencyByLemma == null) {
                    return new ArrayList<>();
                }
                log.info("frequency " + foundFrequencyByLemma);
                double percentage = (foundFrequencyByLemma / totalPages) * 100;
                log.info("percentage " + percentage);
                if (percentage > leastPercentage) {
                    wordsWithFrequencies.put(frequenciesWithLemmas.getKey(), percentage);
                    log.info("High lemma found " + frequenciesWithLemmas.getKey());
                }
            }
            Map<String, Double> lemmasInDesc = getLemmasInDesc(wordsWithFrequencies);
            Set<String> getLemma = lemmasInDesc.keySet();
            AbsoluteRelevanceForOneSite absoluteRelevanceForOneSite = getAbsoluteRelevanceByLemmaAndSiteId(getLemma, siteId);
            setSearchDtoForOneSite(configuredSearch, absoluteRelevanceForOneSite.page(), site, absoluteRelevanceForOneSite.absoluteRelevance(), searchDtoList);
        }
        return searchDtoList.stream().sorted(Comparator.comparing(SearchDto::getRelevance, Comparator.reverseOrder())).toList();
    }

    private AbsoluteRelevanceForOneSite getAbsoluteRelevanceByLemmaAndSiteId(Set<String> getLemma, Integer siteId) {
        List<Integer> lemmaId = lemmaRepository.findLemmaIdsByLemmaAndSiteId(getLemma, siteId);
        List<Integer> pageIds = indexRepository.findPageIdsByLemmaIds(lemmaId);
        List<Long> absoluteRelevance = indexRepository.findRankPerPageByPageIds(pageIds);
        List<Page> page = pageRepository.findPageBySiteIdAndPageId(pageIds, siteId);
        return new AbsoluteRelevanceForOneSite(absoluteRelevance, page);
    }


    @Override
    public List<SearchDto> searchByAllSites(ConfiguredSearch configuredSearch) throws IOException {
        List<SearchDto> searchDtoList = new ArrayList<>();
        Map<String, Integer> filteredLemmas = Lemmatizater.splitTextIntoWords(configuredSearch.getQuery());
        Map<String, Double> wordsWithFrequencies = new HashMap<>();
        double countFreq = 0;
        for (Map.Entry<String, Integer> frequenciesWithLemmas : filteredLemmas.entrySet()) {
            log.info("WORD " + frequenciesWithLemmas.getKey());
            long totalPages = pageRepository.countByPage();
            log.info("total pages " + totalPages);
            List<Double> foundFrequencyByLemma = lemmaRepository.findFrequencyByLemma(frequenciesWithLemmas.getKey());
            if (foundFrequencyByLemma == null) {
                return new ArrayList<>();
            }
            for (Double v : foundFrequencyByLemma) {
                countFreq += v;
            }
            double percentage = (countFreq / totalPages) * 100;
            log.info("percentage " + percentage);
            wordsWithFrequencies.put(frequenciesWithLemmas.getKey(), percentage);
            log.info("High lemma found " + frequenciesWithLemmas.getKey());
        }
        Map<String, Double> lemmasInDesc = getLemmasInDesc(wordsWithFrequencies);
        Set<String> getLemma = lemmasInDesc.keySet();
        AbsoluteRelevanceForAllSites relevancePerPage = getAbsoluteRelevanceByLemmas(getLemma);
        setSearchDtoForAllSites(configuredSearch, relevancePerPage.page(), relevancePerPage.absoluteRelevance(), searchDtoList);
        return searchDtoList.stream().sorted(Comparator.comparing(SearchDto::getRelevance, Comparator.reverseOrder())).toList();
    }

    private AbsoluteRelevanceForAllSites getAbsoluteRelevanceByLemmas(Set<String> getLemma) {
        List<Integer> lemmaId = lemmaRepository.findLemmaIdsByLemmas(getLemma);
        List<Integer> pageIds = indexRepository.findPageIdsByLemmaIds(lemmaId);
        List<Long> absoluteRelevance = indexRepository.findRankPerPageByPageIds(pageIds);
        List<Page> page = pageRepository.findPageByPageId(pageIds);
        return new AbsoluteRelevanceForAllSites(absoluteRelevance, page);
    }


    private Map<String, Double> getLemmasInDesc(Map<String, Double> wordsWithFrequencies) {
        return wordsWithFrequencies.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.naturalOrder())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }

    private void setSearchDtoForOneSite(ConfiguredSearch configuredSearch, List<Page> page, Site site, List<Long> absoluteRelevance, List<SearchDto> searchDtoList) throws IOException {
        for (int i = 0; i < page.size(); i++) {
            SearchDto searchDto = new SearchDto();
            searchDto.setSiteName(site.getName());
            log.info("PAGES FOUND " + page.get(i).getPath());
            searchDto.setSite(configuredSearch.getSite());
            searchDto.setUri(page.get(i).getPath());
            Document doc = Jsoup.connect(configuredSearch.getSite() + page.get(i).getPath()).get();
            setSearchDtoList(configuredSearch, absoluteRelevance, searchDtoList, i, searchDto, doc);
        }
    }

    private void setSearchDtoList(ConfiguredSearch configuredSearch, List<Long> absoluteRelevance, List<SearchDto> searchDtoList, int i, SearchDto searchDto, Document doc) {
        String title = doc.title();
        searchDto.setTitle(title);
        String snippet = "";
        Element element = doc.select(":containsOwn(" + configuredSearch.getQuery() + ")").first();
        if(element!=null)
        {
            snippet = element.text().toLowerCase();
        }
        int indexOfTheSearchedWord = snippet.indexOf(configuredSearch.getQuery().toLowerCase());
        if (indexOfTheSearchedWord != -1) {
            String s = snippet
                    .substring(indexOfTheSearchedWord)
                    .replace(configuredSearch.getQuery().toLowerCase(), "<b>" + configuredSearch.getQuery().toLowerCase() + "</b>")
                    .replaceAll("<br>", " ");
            int lengthOfText = Math.min(240, s.length());
            System.out.println(lengthOfText);
            StringBuilder st = new StringBuilder();
            String lines = s.substring(0, lengthOfText);
            st.append(lines);
            searchDto.setSnippet(st.toString());
        }
        double relevance = (double) (absoluteRelevance.get(i) / Collections.max(absoluteRelevance));
        searchDto.setRelevance(relevance);
        log.info("RELEVANCE " + relevance);
        log.info("SNIPPET " + searchDto.getSnippet());
        if (searchDto.getSnippet() != null && !searchDto.getSnippet().isEmpty()) {
            searchDtoList.add(searchDto);
        }
    }


    private void setSearchDtoForAllSites(ConfiguredSearch configuredSearch, List<Page> page, List<Long> absoluteRelevance, List<SearchDto> searchDtoList) throws
            IOException {
        for (int i = 0; i < page.size(); i++) {
            SearchDto searchDto = new SearchDto();
            Site site = siteRepository.findSiteById(page.get(i).getSite().getId());
            searchDto.setSite(site.getUrl() + page.get(i).getPath());
            searchDto.setSiteName(site.getName());
            log.info("PAGES FOUND " + page.get(i).getPath());
            searchDto.setUri(page.get(i).getPath());
            Document doc = Jsoup.connect(site.getUrl() + page.get(i).getPath()).get();
            setSearchDtoList(configuredSearch, absoluteRelevance, searchDtoList, i, searchDto, doc);
        }
    }
}









