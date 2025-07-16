package ma.laraki.ebankingbackend.web;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import ma.laraki.ebankingbackend.dtos.*;
import ma.laraki.ebankingbackend.exceptions.BalanceNotSufficientException;
import ma.laraki.ebankingbackend.exceptions.BankAccountNotFoundException;
import ma.laraki.ebankingbackend.exceptions.CustomerNotFoundException;
import ma.laraki.ebankingbackend.services.BankAccountService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@SecurityRequirement(name = "BearerAuth")

@RestController
@CrossOrigin("*")
@RequestMapping("/accounts")
public class BankAccountRestController {
    private BankAccountService bankAccountService;
    public BankAccountRestController(BankAccountService bankAccountService){this.bankAccountService=bankAccountService;}

    @GetMapping("")
    public List<BankAccountDTO>  listAccounts(){
        return bankAccountService.bankAccountList();
    }

    @GetMapping("/{accountId}")
    public BankAccountDTO getBankAccount(@PathVariable(name = "accountId") String accountId) throws BankAccountNotFoundException {
        return bankAccountService.getBankAccount(accountId);
    }

    @GetMapping("/{accountId}/operations")
    public List<AccountOperationDTO> getHistory(@PathVariable(name = "accountId") String accountId){
        return bankAccountService.accountHistory(accountId);
    }

    @GetMapping("/{accountId}/pageOperations")
        public AccountHistoryDTO getAccountHistory(@PathVariable(name = "accountId") String accountId,
                                                   @RequestParam(name = "page",defaultValue = "0") int page,
                                                   @RequestParam(name = "size",defaultValue = "5") int size
    ) throws BankAccountNotFoundException {
            return bankAccountService.getAccountHistory(accountId,page,size);
    }

    @PostMapping("/debit")
    public DebitDTO debit(@RequestBody DebitDTO debitDTO) throws BankAccountNotFoundException, BalanceNotSufficientException {
         this.bankAccountService.debit(debitDTO.getAccountId(),debitDTO.getAmount(),debitDTO.getDescription());
         return debitDTO;
    }
    @PostMapping("/credit")
    public CreditDTO credit(@RequestBody CreditDTO creditDTO) throws BankAccountNotFoundException {
        this.bankAccountService.credit(creditDTO.getAccountId(),creditDTO.getAmount(),creditDTO.getDescription());
        return creditDTO;
    }

    @PostMapping("/transfer")
    public void  Transfer(@RequestBody TransferRequestDTO transferRequestDTO) throws BankAccountNotFoundException, BalanceNotSufficientException {
        this.bankAccountService.transfer(transferRequestDTO.getAccountSource(),transferRequestDTO.getAccountDestination(),transferRequestDTO.getAmount());
    }

    @GetMapping("/customer/{customerId}")
    public List<BankAccountDTO> getBankAccountByCustomerId(@PathVariable(name = "customerId") Long customerId){
        return this.bankAccountService.BankAccountsByCustomerId(customerId);
    }

    @PostMapping("/customer/{customerId}/current")
    public CurrentBankAccountDTO saveCurrentBankAccount(@PathVariable(name = "customerId") Long customerId,@RequestBody CurrentAccountRequestDTO currentAccountRequestDTO) throws CustomerNotFoundException {
        return this.bankAccountService.saveCurrentBankAccount(currentAccountRequestDTO.getBalance(),currentAccountRequestDTO.getOverDraft(),customerId);
    }
    @PostMapping("/customer/{customerId}/saving")
    public SavingBankAccountDTO saveSavingBankAccount(@PathVariable(name = "customerId") Long customerId,@RequestBody SavingAccountRequestDTO savingAccountRequestDTO) throws CustomerNotFoundException {
        return this.bankAccountService.saveSavingBankAccount(savingAccountRequestDTO.getBalance(),savingAccountRequestDTO.getInterestRate(),customerId);
    }
}
