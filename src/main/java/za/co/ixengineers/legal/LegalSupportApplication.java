package za.co.ixengineers.legal;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
@SpringBootApplication
@EnableScheduling
public class LegalSupportApplication{
    public static void main(String[] args){
        SpringApplication.run(LegalSupportApplication.class,args);
    }
}
