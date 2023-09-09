package de.wigeogis.pmedian.config;

import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Frontend frontend;
    private Mapping mapping;

    @Getter
    @Setter
    public static class Frontend {
        private String url;
    }

    @Getter
    @Setter
    public static class Mapping {
        private String style;
    }

    public void setFrontend(Frontend frontend) {
        this.frontend = frontend;
    }

    public void setMapping(Mapping mapping) {
        this.mapping = mapping;
    }
}
