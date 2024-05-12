package searchengine.Responses;

import lombok.Data;
import searchengine.dto.searchDto.SearchDto;
import java.util.List;

@Data
public class SearchResponse {

    private Boolean result;
    private int count;
    private List<SearchDto> data;
    String error;

    public SearchResponse(Boolean result, int count, List<SearchDto> data) {
        this.result = result;
        this.count = count;
        this.data = data;
    }

}
