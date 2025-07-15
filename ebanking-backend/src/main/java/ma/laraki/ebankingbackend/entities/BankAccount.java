package ma.laraki.ebankingbackend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.laraki.ebankingbackend.enums.AccountStatus;
import java.util.Date;
import java.util.List;
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name ="TYPE",length = 4,discriminatorType = DiscriminatorType.STRING) // par defaut string
@Data
@NoArgsConstructor
@AllArgsConstructor
public   class BankAccount {
    @Id
    private String id;
    private double balance;
    private Date createdAt;
    @Enumerated(EnumType.STRING)
    private AccountStatus status;
    @ManyToOne //plusieurs compte pour un client
    private Customer customer; // un client peut avoir plusieurs accounts
    // FetchType.LAZY=>va pas charger list account operation va charger just l'attribute de bankAccount
    // FetchType.EAGER=> va  charger list account operation avec  l'attribute de bankAccount (danger => pas utiliser eager si tu sera pas utiliser stockage de mémoire)
    @OneToMany(mappedBy = "bankAccount",fetch = FetchType.LAZY) // mappedBy c'est a dire que bankAccount est aussi mapper dans la classe AccountOperation
    private List<AccountOperation> accountOperations; // un compte peut avoir plusieurs operations
}
