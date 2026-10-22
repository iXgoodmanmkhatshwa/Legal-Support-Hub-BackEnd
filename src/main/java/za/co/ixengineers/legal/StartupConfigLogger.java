package za.co.ixengineers.legal;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class StartupConfigLogger {

    @Value("${spring.datasource.url:NOT_SET}")
    private String datasourceUrl;

    @PostConstruct
    public void log() {
        System.out.println(
                "DATASOURCE URL = [" +
                        datasourceUrl +
                        "]"
        );
    }
}