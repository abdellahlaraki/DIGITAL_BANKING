package ma.laraki.ebankingbackend.dtos;

import lombok.Data;

@Data
public class SavingAccountRequestDTO {
    private double balance;
    private double interestRate;
}
