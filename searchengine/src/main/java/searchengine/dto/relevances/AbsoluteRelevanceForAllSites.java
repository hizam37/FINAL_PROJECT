package searchengine.dto.relevances;

import searchengine.model.Page;
import java.util.List;

public record AbsoluteRelevanceForAllSites(List<Long> absoluteRelevance, List<Page> page) {
}