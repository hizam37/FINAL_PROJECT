package searchengine.Responses;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FalseResponse {
    private Boolean result;
    private String error;
}
