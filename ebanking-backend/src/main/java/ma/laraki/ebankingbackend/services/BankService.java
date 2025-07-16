package ma.laraki.ebankingbackend.services;

import ma.laraki.ebankingbackend.entities.BankAccount;
import ma.laraki.ebankingbackend.entities.CurrentAccount;
import ma.laraki.ebankingbackend.entities.SavingAccount;
import ma.laraki.ebankingbackend.repositories.BankAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BankService {



    private final BankAccountRepository bankAccountRepository;
    public BankService(BankAccountRepository bankAccountRepository){
        this.bankAccountRepository=bankAccountRepository;
    }
    // méthodes transactionnel
    public void consulter(){
        BankAccount bankAccount=bankAccountRepository.findById("04a7ca13-db53-408b-aa1e-cbe057a3f3f5").orElse(null);
        if (bankAccount!=null){
            System.out.println("*************************");
            System.out.println(bankAccount.getId());
            System.out.println(bankAccount.getBalance());
            System.out.println(bankAccount.getStatus());
            System.out.println(bankAccount.getCreatedAt());
            System.out.println(bankAccount.getCustomer().getName());
            System.out.println(bankAccount.getClass().getSimpleName());
            if(bankAccount instanceof CurrentAccount){
                System.out.println("OverDraft=> "+((CurrentAccount)bankAccount).getOverDraft());
            }
            else if (bankAccount instanceof SavingAccount) {
                System.out.println("Rate=> "+((SavingAccount)bankAccount).getInterestRate());
            }

            bankAccount.getAccountOperations().forEach(accountOperation -> {
                System.out.println("====================================");
                System.out.println(accountOperation.getType());
                System.out.println(accountOperation.getAmount());
                System.out.println(accountOperation.getOperationDate());
            });
        }
    }
}
