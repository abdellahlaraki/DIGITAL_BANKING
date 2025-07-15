package ma.laraki.ebankingbackend.dtos;

import lombok.Data;

@Data
public class CurrentAccountRequestDTO {
    private double balance;
    private double overDraft;
}
