package searchengine.services;

import searchengine.config.ConfiguredSearch;
import searchengine.dto.searchDto.SearchDto;
import java.io.IOException;
import java.util.List;

public interface SearchService {

    List<SearchDto> searchBySite(ConfiguredSearch configuredSearch) throws IOException, InterruptedException;
    List<SearchDto> searchByAllSites(ConfiguredSearch configuredSearch) throws IOException, InterruptedException;

}
