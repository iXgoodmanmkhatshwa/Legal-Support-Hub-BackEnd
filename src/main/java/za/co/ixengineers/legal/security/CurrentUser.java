package za.co.ixengineers.legal.security;
import org.springframework.security.oauth2.jwt.Jwt;
import java.util.*;
public record CurrentUser(String id,String name,String email,Set<String> roles){
    public static CurrentUser from(Jwt j){
        var raw=j.getClaimAsStringList("roles");
        return new CurrentUser(Optional.ofNullable(j.getSubject()).orElse("unknown"),
                Optional.ofNullable(j.getClaimAsString("name")).orElse("Unknown user"),
                Optional.ofNullable(j.getClaimAsString("preferred_username")).orElse(""),
                raw==null?Set.of():Set.copyOf(raw));
    }
    public boolean has(String role){
        return roles.contains(role);
    }
}
