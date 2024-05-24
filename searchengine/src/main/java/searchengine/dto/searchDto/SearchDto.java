package searchengine.dto.searchDto;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class SearchDto {
    private String site;
    private String siteName;
    private String uri;
    private String title;
    private String snippet;
    private double relevance;
}
