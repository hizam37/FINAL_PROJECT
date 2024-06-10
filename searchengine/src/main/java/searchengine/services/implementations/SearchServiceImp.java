package searchengine.services.implementations;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
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
import searchengine.services.ConnecterService;
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
    private final ConnecterService connecterService;

    @Override
    public List<SearchDto> searchBySite(ConfiguredSearch configuredSearch) throws IOException, InterruptedException {
        List<SearchDto> searchDtoList = new ArrayList<>();
        Site site = siteRepository.findSiteByUrl(configuredSearch.getSite());
        if (site.getUrl().equals(configuredSearch.getSite())) {
            Integer siteId = siteRepository.getIdByUrl(site.getUrl());
            Map<String, Integer> filteredLemmas = Lemmatizater.splitTextIntoWords(configuredSearch.getQuery());
            Map<String, Double> wordsWithFrequencies = new HashMap<>();
            for (Map.Entry<String, Integer> frequenciesWithLemmas : filteredLemmas.entrySet()) {
                long totalPages = pageRepository.countPageBySiteId(siteId);
                Double foundFrequencyByLemma = lemmaRepository.findFrequencyByLemmaAndSiteId(frequenciesWithLemmas.getKey(), siteId);
                if (foundFrequencyByLemma == null) {
                    return new ArrayList<>();
                }
                double percentage = (foundFrequencyByLemma / totalPages) * 100;
                wordsWithFrequencies.put(frequenciesWithLemmas.getKey(), percentage);
                log.info("High lemma found " + frequenciesWithLemmas.getKey());
            }
            Map<String, Double> lemmasInDesc = getLemmasInDesc(wordsWithFrequencies);
            Set<String> getLemma = lemmasInDesc.keySet();
            AbsoluteRelevanceForOneSite absoluteRelevanceForOneSite = getAbsoluteRelevanceByLemmaAndSiteId(getLemma, siteId);
            setSearchDto(configuredSearch, absoluteRelevanceForOneSite.page(), absoluteRelevanceForOneSite.absoluteRelevance(), searchDtoList);
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
    public List<SearchDto> searchByAllSites(ConfiguredSearch configuredSearch) throws IOException, InterruptedException {
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
            wordsWithFrequencies.put(frequenciesWithLemmas.getKey(), percentage);
        }
        Map<String, Double> lemmasInDesc = getLemmasInDesc(wordsWithFrequencies);
        Set<String> getLemma = lemmasInDesc.keySet();
        AbsoluteRelevanceForAllSites relevancePerPage = getAbsoluteRelevanceByLemmas(getLemma);
        setSearchDto(configuredSearch, relevancePerPage.page(), relevancePerPage.absoluteRelevance(), searchDtoList);
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

    private void setSearchDto(ConfiguredSearch configuredSearch, List<Page> page, List<Long> absoluteRelevance, List<SearchDto> searchDtoList) {
        for (int i = 0; i < page.size(); i++) {
            SearchDto searchDto = new SearchDto();
            Site site = siteRepository.findSiteById(page.get(i).getSite().getId());
            searchDto.setSiteName(site.getName());
            searchDto.setSite(page.get(i).getSite().getUrl());
            searchDto.setUri(page.get(i).getPath());
            Document doc = Jsoup.parse(page.get(i).getContent());
            double relevance = (double) (absoluteRelevance.get(i) / Collections.max(absoluteRelevance));
            searchDto.setRelevance(relevance);
            String title = doc.title();
            searchDto.setTitle(title);
            setSnippetsInSearchDtoList(configuredSearch, searchDtoList, searchDto, doc);
        }
    }

    private void setSnippetsInSearchDtoList(ConfiguredSearch configuredSearch, List<SearchDto> searchDtoList, SearchDto searchDto, Document doc) {
        setSnippet(configuredSearch, searchDto, doc);
        if (searchDto.getSnippet() != null && !searchDto.getSnippet().isEmpty()) searchDtoList.add(searchDto);
    }


    private void setSnippet(ConfiguredSearch configuredSearch, SearchDto searchDto, Document doc) {
        String snippet = "";
        Element element = doc.select(":containsOwn(" + configuredSearch.getQuery() + ")").first();
        if (element != null) {
            snippet = element.text().toLowerCase();
        }
        int indexOfTheSearchedWord = snippet.indexOf(configuredSearch.getQuery().toLowerCase());
        if (indexOfTheSearchedWord != -1) {
            String s = snippet
                    .substring(indexOfTheSearchedWord)
                    .replace(configuredSearch.getQuery().toLowerCase(), "<b>" + configuredSearch.getQuery().toLowerCase() + "</b>")
                    .replaceAll("<br>", " ");
            StringBuilder fixedLetter = new StringBuilder();
            boolean capitalizeNext = false;
            linesFormatter(s, capitalizeNext, fixedLetter);
            String[] words = fixedLetter.toString().split(" ");
            StringBuilder line = new StringBuilder();
            String createdSnippet = getLimitedLines(words, line);
            searchDto.setSnippet(createdSnippet);
        }
    }

    @NotNull
    private String getLimitedLines(String[] words, StringBuilder line) {
        StringBuilder limitedLine = new StringBuilder();
        int lineCount = 0;
        for (String word : words) {
            if (line.length() + word.length() > 90) {
                limitedLine.append(line).append("\n");
                line = new StringBuilder();
                lineCount++;
                if (lineCount == 3) {
                    break;
                }
            }
            line.append(word).append(" ");
        }
        limitedLine.append(line);
        return "<b>" + limitedLine.substring(3, 4).toUpperCase() + limitedLine.substring(4);
    }

    private void linesFormatter(String s, boolean capitalizeNext, StringBuilder fixedLetter) {
        for (char l : s.toCharArray()) {
            if (Character.isLetter(l) && l != 'b') {
                if (capitalizeNext) {
                    fixedLetter.append(Character.toUpperCase(l));
                    capitalizeNext = false;
                } else {
                    fixedLetter.append(l);
                }
            } else {
                fixedLetter.append(l);
                if (l == '.') {
                    capitalizeNext = true;
                }
            }
        }
    }

}







