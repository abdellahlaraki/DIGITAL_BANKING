package ma.laraki.ebankingbackend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.laraki.ebankingbackend.enums.OperationType;
import java.util.Date;
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountOperation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Date operationDate;
    private double amount;
    @Enumerated(EnumType.STRING) // STRING or ORDINAL
    private OperationType type;
    @ManyToOne
    private BankAccount bankAccount; // chaque operation appartient a un compte
    private String description;
}
