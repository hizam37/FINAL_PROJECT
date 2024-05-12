package searchengine.Responses;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class IndexResponse {
    private Boolean result;
    private String error;
    public IndexResponse(Boolean result) {
        this.result = result;
    }
}
