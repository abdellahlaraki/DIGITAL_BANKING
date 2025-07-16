package ma.laraki.ebankingbackend.dtos;

import lombok.Data;

import java.util.List;
@Data
public class UserCreationDTO {
    private String username;
    private String password;
    private List<String> roles;  // list of role names
}
