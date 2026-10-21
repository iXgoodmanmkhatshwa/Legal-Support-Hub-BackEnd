package za.co.ixengineers.legal.document;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.nio.file.*;
import java.util.*;
@Component
@Profile("!graph")
public class LocalDocumentStorage implements DocumentStorage{
    private final Path root=Path.of(System.getenv().getOrDefault("DOCUMENT_STORAGE_PATH","/tmp/legal-documents"));
    public StoredDocument store(String ref,MultipartFile f)
            throws IOException{Path dir=root.resolve(ref);
        Files.createDirectories(dir);
        String safe=UUID.randomUUID()+"-"+Path.of(Objects.requireNonNullElse(f.getOriginalFilename(),"file")).getFileName();
        Path p=dir.resolve(safe);
        try(InputStream in=f.getInputStream()){
            Files.copy(in,p,StandardCopyOption.REPLACE_EXISTING);
        }
        return new StoredDocument(p.toString(),null);
    }
}
