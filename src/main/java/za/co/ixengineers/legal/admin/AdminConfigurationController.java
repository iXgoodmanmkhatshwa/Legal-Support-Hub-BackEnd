package za.co.ixengineers.legal.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import za.co.ixengineers.legal.security.CurrentUser;

import static za.co.ixengineers.legal.admin.AdminConfigurationDtos.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminConfigurationController {

    private final AdminConfigurationService service;

    @GetMapping("/configuration")
    @PreAuthorize(
            "hasAnyRole(" +
                    "'SYSTEM_ADMIN'," +
                    "'HEAD_OF_LEGAL'," +
                    "'LEGAL_ADMIN'" +
                    ")"
    )
    public ConfigurationSummaryResponse configuration() {
        return service.getConfiguration();
    }

    @PutMapping("/deadline-rules/{ruleId}")
    @PreAuthorize(
            "hasAnyRole(" +
                    "'SYSTEM_ADMIN'," +
                    "'HEAD_OF_LEGAL'," +
                    "'LEGAL_ADMIN'" +
                    ")"
    )
    public DeadlineRuleResponse updateDeadlineRule(
            @PathVariable
            String ruleId,

            @Valid
            @RequestBody
            UpdateDeadlineRuleRequest request,

            @AuthenticationPrincipal
            Jwt jwt
    ) {
        return service.updateDeadlineRule(
                ruleId,
                request,
                CurrentUser.from(jwt)
        );
    }

    @PostMapping("/configuration/publish")
    @PreAuthorize(
            "hasAnyRole(" +
                    "'SYSTEM_ADMIN'," +
                    "'HEAD_OF_LEGAL'" +
                    ")"
    )
    public PublishConfigurationResponse publish(
            @Valid
            @RequestBody
            PublishConfigurationRequest request,

            @AuthenticationPrincipal
            Jwt jwt
    ) {
        return service.publish(
                request,
                CurrentUser.from(jwt)
        );
    }
}