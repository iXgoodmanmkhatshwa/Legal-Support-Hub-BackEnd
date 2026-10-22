package za.co.ixengineers.legal.matter;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import za.co.ixengineers.legal.security.CurrentUser;

import java.io.IOException;

import static za.co.ixengineers.legal.matter.Domain.*;
import static za.co.ixengineers.legal.matter.MatterDtos.*;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/matters")
@RequiredArgsConstructor
public class MatterController {

    private final MatterService service;

    @GetMapping
    Page<MatterResponse> list(
            @AuthenticationPrincipal Jwt jwt,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return service.list(
                CurrentUser.from(jwt),
                pageable
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    MatterResponse create(
            @Valid
            @RequestBody
            CreateMatterRequest request,

            @AuthenticationPrincipal
            Jwt jwt
    ) {
        return service.create(
                request,
                CurrentUser.from(jwt)
        );
    }

    @PostMapping("/{ref}/assignments")
    @PreAuthorize("hasAnyRole('LEGAL_ADMIN','HEAD_OF_LEGAL','SYSTEM_ADMIN')")
    MatterResponse assign(
            @PathVariable
            String ref,

            @Valid
            @RequestBody
            AssignmentRequest request,

            @AuthenticationPrincipal
            Jwt jwt
    ) {
        return service.assign(ref, request, CurrentUser.from(jwt));
    }

    @PatchMapping("/{ref}/status")
    @PreAuthorize(
            "hasAnyRole(" +
                    "'LEGAL_ADMIN'," +"'SYSTEM_ADMIN',"+
                    "'LEGAL_USER'," +
                    "'HEAD_OF_LEGAL'" +
                    ")"
    )
    MatterResponse status(
            @PathVariable
            String ref,

            @Valid
            @RequestBody
            StatusRequest request,

            @AuthenticationPrincipal
            Jwt jwt
    ) {
        return service.status(ref, request, CurrentUser.from(jwt));
    }



    private MediaType safeMediaType(String contentType) {
        if (contentType == null ||
                contentType.isBlank()) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }

        try {
            return MediaType.parseMediaType(contentType);
        } catch (IllegalArgumentException exception) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }


    @PostMapping(
            path = "/{ref}/documents",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    DocumentResponse document(
            @PathVariable String ref,
            @RequestParam DocumentCategory category,
            @RequestPart("file") MultipartFile file,
            @AuthenticationPrincipal Jwt jwt
    ) throws IOException {
        return service.upload(
                ref,
                category,
                file,
                CurrentUser.from(jwt)
        );
    }

    @GetMapping("/{ref}/documents")
    @PreAuthorize("""
    hasAnyRole(
        'SYSTEM_ADMIN',
        'HEAD_OF_LEGAL',
        'LEGAL_ADMIN',
        'LEGAL_USER'
    )
    """)
    List<DocumentResponse> listDocuments(
            @PathVariable String ref,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return service.listDocuments(
                ref,
                CurrentUser.from(jwt)
        );
    }

    @GetMapping("/{ref}/documents/{documentId}/content")
    @PreAuthorize("""
    hasAnyRole(
        'SYSTEM_ADMIN',
        'HEAD_OF_LEGAL',
        'LEGAL_ADMIN',
        'LEGAL_USER'
    )
    """)
    ResponseEntity<Resource> downloadDocument(
            @PathVariable String ref,
            @PathVariable UUID documentId,
            @AuthenticationPrincipal Jwt jwt
    ) throws IOException {

        DownloadedDocument document =
                service.downloadDocument(
                        ref,
                        documentId,
                        CurrentUser.from(jwt)
                );

        MediaType contentType =
                safeMediaType(document.contentType());

        return ResponseEntity.ok()
                .contentType(contentType)
                .contentLength(document.contentLength())
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition
                                .attachment()
                                .filename(
                                        document.fileName(),
                                        StandardCharsets.UTF_8
                                )
                                .build()
                                .toString()
                )
                .body(document.resource());
    }


    @PostMapping("/{ref}/feedback")
    @ResponseStatus(HttpStatus.CREATED)
    void feedback(
            @PathVariable
            String ref,

            @Valid
            @RequestBody
            FeedbackRequest request,

            @AuthenticationPrincipal
            Jwt jwt
    ) {
        service.addFeedback(
                ref,
                request,
                CurrentUser.from(jwt)
        );
    }
}