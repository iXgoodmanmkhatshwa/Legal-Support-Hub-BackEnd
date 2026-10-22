package za.co.ixengineers.legal.document;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import org.springframework.core.io.Resource;
public interface DocumentStorage{
    StoredDocument store(String matterReference,MultipartFile file)throws IOException;
    record StoredDocument(String externalId,String webUrl){}
    Resource loadAsResource(
            String externalId
    ) throws IOException;
}
