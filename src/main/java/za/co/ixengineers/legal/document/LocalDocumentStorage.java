package za.co.ixengineers.legal.document;

import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

@Component
@Profile("!graph")
public class LocalDocumentStorage implements DocumentStorage {

    private final Path root = Path.of(
            System.getenv().getOrDefault(
                    "DOCUMENT_STORAGE_PATH",
                    "/tmp/legal-documents"
            )
    );

    @Override
    public StoredDocument store(
            String reference,
            MultipartFile file
    ) throws IOException {

        Path directory = root.resolve(reference);

        Files.createDirectories(directory);

        String safeFileName =
                UUID.randomUUID()
                        + "-"
                        + Path.of(
                        Objects.requireNonNullElse(
                                file.getOriginalFilename(),
                                "file"
                        )
                ).getFileName();

        Path path =
                directory.resolve(safeFileName);

        try (InputStream input =
                     file.getInputStream()) {

            Files.copy(
                    input,
                    path,
                    StandardCopyOption.REPLACE_EXISTING
            );
        }

        return new StoredDocument(
                path.toString(),
                null
        );
    }

    @Override
    public Resource loadAsResource(
            String externalId
    ) throws IOException {

        Path file = Path.of(externalId);

        try {
            Resource resource =
                    new UrlResource(
                            file.toUri()
                    );

            if (
                    resource.exists()
                            && resource.isReadable()
            ) {
                return resource;
            }

            throw new FileNotFoundException(
                    "Document not found: "
                            + externalId
            );

        } catch (MalformedURLException ex) {
            throw new IOException(
                    "Could not load document: "
                            + externalId,
                    ex
            );
        }
    }
}