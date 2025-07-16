package ma.laraki.ebankingbackend.repositories;

import ma.laraki.ebankingbackend.entities.BankAccount;
import ma.laraki.ebankingbackend.entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BankAccountRepository extends JpaRepository<BankAccount,String> {
    List<BankAccount> findByCustomer_Id(Long customerId);
}
