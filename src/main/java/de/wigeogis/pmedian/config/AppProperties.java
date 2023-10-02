package de.wigeogis.pmedian.config;

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

    public void setFrontend(Frontend frontend) {
        this.frontend = frontend;
    }

    public void setMapping(Mapping mapping) {
        this.mapping = mapping;
    }

    @Getter
    @Setter
    public static class Frontend {
        private String url;
    }

    @Getter
    @Setter
    public static class Mapping {
        private String url;
        private String style;
    }
}
