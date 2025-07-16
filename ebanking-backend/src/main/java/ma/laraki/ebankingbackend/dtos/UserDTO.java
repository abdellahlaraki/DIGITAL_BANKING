package ma.laraki.ebankingbackend.dtos;

import lombok.Data;
import ma.laraki.ebankingbackend.entities.Role;
import ma.laraki.ebankingbackend.entities.User;

import java.util.List;
import java.util.stream.Collectors;
@Data
public class UserDTO {
    private Long id;
    private String username;
    private boolean enabled;
    private List<String> roles;

    public UserDTO(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.enabled = user.isEnabled();
        this.roles = user.getRoles().stream()
                .map(Role::getRoleName)
                .collect(Collectors.toList());
    }
}
