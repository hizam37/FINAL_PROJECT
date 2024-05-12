package searchengine.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "search-settings")
public class ConfiguredSearch {
    private String query;
    private String site;
    private int limit;
    private int offset;
}
