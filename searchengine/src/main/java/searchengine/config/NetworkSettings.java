package searchengine.config;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties("network-settings")
public class NetworkSettings {
    private List<UserAgents> userAgents;
    private String referrer;
    private Integer timeout;
}

