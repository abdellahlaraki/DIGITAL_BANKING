package ma.laraki.ebankingbackend.dtos;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.laraki.ebankingbackend.entities.BankAccount;
import ma.laraki.ebankingbackend.enums.OperationType;

import java.util.Date;


@Data

public class AccountOperationDTO {

    private Long id;
    private Date operationDate;
    private double amount;
    private OperationType type;
    private String description;
}
