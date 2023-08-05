package ua.dtsebulia.backend.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.dtsebulia.backend.user.Role;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegistrationRequest {

    private String firstname;
    private String lastname;
    private String email;
    private String password;
    private Role role;

}
