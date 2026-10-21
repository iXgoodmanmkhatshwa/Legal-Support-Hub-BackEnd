package za.co.ixengineers.legal.contract;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import za.co.ixengineers.legal.security.CurrentUser;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import static za.co.ixengineers.legal.contract.ContractDtos.*;

@RestController
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
public class ContractController {

    private final ContractService service;

    @GetMapping
    @PreAuthorize(
            "hasAnyRole(" +
                    "'SYSTEM_ADMIN'," +
                    "'HEAD_OF_LEGAL'," +
                    "'LEGAL_ADMIN'," +
                    "'LEGAL_USER'" +
                    ")"
    )
    public List<ContractResponse> list() {
        return service.list();
    }

    @PostMapping(
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(
            "hasAnyRole(" +
                    "'SYSTEM_ADMIN'," +
                    "'HEAD_OF_LEGAL'," +
                    "'LEGAL_ADMIN'," +
                    "'LEGAL_USER'" +
                    ")"
    )
    public ContractResponse create(
            @RequestParam
            String title,

            @RequestParam
            String counterparty,

            @RequestParam(required = false)
            String contractType,

            @RequestParam(required = false)
            LocalDate effectiveDate,

            @RequestParam(required = false)
            LocalDate expiryDate,

            @RequestParam(defaultValue = "false")
            boolean autoRenewal,

            @RequestParam(required = false)
            Integer noticePeriodDays,

            @RequestPart("primaryFile")
            MultipartFile primaryFile,

            @RequestPart(
                    value = "annexures",
                    required = false
            )
            List<MultipartFile> annexures,

            @AuthenticationPrincipal
            Jwt jwt
    ) throws IOException {
        return service.create(
                title,
                counterparty,
                contractType,
                effectiveDate,
                expiryDate,
                autoRenewal,
                noticePeriodDays,
                primaryFile,
                annexures,
                CurrentUser.from(jwt)
        );
    }

    @PutMapping("/{reference}/confirmation")
    @PreAuthorize(
            "hasAnyRole(" +
                    "'SYSTEM_ADMIN'," +
                    "'HEAD_OF_LEGAL'," +
                    "'LEGAL_ADMIN'," +
                    "'LEGAL_USER'" +
                    ")"
    )
    public ContractResponse confirm(
            @PathVariable
            String reference,

            @Valid
            @RequestBody
            ConfirmContractRequest request,

            @AuthenticationPrincipal
            Jwt jwt
    ) {
        return service.confirm(
                reference,
                request,
                CurrentUser.from(jwt)
        );
    }
}