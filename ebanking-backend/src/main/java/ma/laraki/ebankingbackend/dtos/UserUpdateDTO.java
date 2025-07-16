package ma.laraki.ebankingbackend.dtos;

import lombok.Data;

import java.util.List;
@Data
public class UserUpdateDTO {
    private String username;
    private boolean enabled;
    private List<String> roles;

}
