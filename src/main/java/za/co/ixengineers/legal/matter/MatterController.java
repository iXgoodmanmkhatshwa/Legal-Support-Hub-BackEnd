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

@RestController
@RequestMapping("/api/matters")
@RequiredArgsConstructor
public class MatterController {

    private final MatterService service;

    @GetMapping
    Page<MatterResponse> list(
            @AuthenticationPrincipal Jwt jwt,

            @PageableDefault(
                    size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {
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
    @PreAuthorize(
            "hasAnyRole('LEGAL_ADMIN','HEAD_OF_LEGAL')"
    )
    MatterResponse assign(
            @PathVariable
            String ref,

            @Valid
            @RequestBody
            AssignmentRequest request,

            @AuthenticationPrincipal
            Jwt jwt
    ) {
        return service.assign(
                ref,
                request,
                CurrentUser.from(jwt)
        );
    }

    @PatchMapping("/{ref}/status")
    @PreAuthorize(
            "hasAnyRole(" +
                    "'LEGAL_ADMIN'," +
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
        return service.status(
                ref,
                request,
                CurrentUser.from(jwt)
        );
    }

    @PostMapping(
            path = "/{ref}/documents",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    DocumentResponse document(
            @PathVariable
            String ref,

            @RequestParam
            DocumentCategory category,

            @RequestPart("file")
            MultipartFile file,

            @AuthenticationPrincipal
            Jwt jwt
    ) throws IOException {
        return service.upload(
                ref,
                category,
                file,
                CurrentUser.from(jwt)
        );
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