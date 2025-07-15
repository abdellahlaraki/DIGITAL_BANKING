package ma.laraki.ebankingbackend.dtos;

import lombok.Data;

import java.util.List;
@Data
public class UserProfileDTO {
    private Long id;
    private String username;
    private boolean enabled;
    private List<String> roles;
}
