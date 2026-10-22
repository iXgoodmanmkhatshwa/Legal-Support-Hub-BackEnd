package za.co.ixengineers.legal.matter;

import org.springframework.core.io.Resource;

public record DownloadedDocument(
        String fileName,
        String contentType,
        long contentLength,
        Resource resource
) {
}
