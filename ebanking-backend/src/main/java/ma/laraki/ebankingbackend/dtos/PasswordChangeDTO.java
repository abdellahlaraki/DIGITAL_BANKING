package ma.laraki.ebankingbackend.dtos;

import lombok.Data;

@Data
public class PasswordChangeDTO {
    private String oldPassword;
    private String newPassword;
}
