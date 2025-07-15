package ma.laraki.ebankingbackend.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.laraki.ebankingbackend.dtos.*;
import ma.laraki.ebankingbackend.entities.*;

import ma.laraki.ebankingbackend.enums.AccountStatus;
import ma.laraki.ebankingbackend.enums.OperationType;
import ma.laraki.ebankingbackend.exceptions.BalanceNotSufficientException;
import ma.laraki.ebankingbackend.exceptions.BankAccountNotFoundException;
import ma.laraki.ebankingbackend.exceptions.CustomerNotFoundException;
import ma.laraki.ebankingbackend.mapper.BankAccountMapperImpl;
import ma.laraki.ebankingbackend.repositories.AccountOperationRepository;
import ma.laraki.ebankingbackend.repositories.BankAccountRepository;
import ma.laraki.ebankingbackend.repositories.CustomerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class BankAccountImpl implements BankAccountService{

    private CustomerRepository customerRepository;
    private BankAccountRepository bankAccountRepository;
    private AccountOperationRepository accountOperationRepository;
    private BankAccountMapperImpl bankAccountMapper;

    @Override
    public CustomerDTO saveCustomer(CustomerDTO customerDTO) {
        log.info("Saving Customer");
        Customer customer=bankAccountMapper.fromCustomerDto(customerDTO);
        Customer saveCustomer=customerRepository.save(customer);
        CustomerDTO savedCustomerDTO=bankAccountMapper.fromCustomer(saveCustomer);
        return savedCustomerDTO;
    }
    @Override
    public CustomerDTO updateCustomer(CustomerDTO customerDTO) {
        log.info("Updating Customer");
        Customer customer=bankAccountMapper.fromCustomerDto(customerDTO);
        Customer updateCustomer=customerRepository.save(customer);
        CustomerDTO updateCustomerDTO=bankAccountMapper.fromCustomer(updateCustomer);
        return updateCustomerDTO;
    }
    @Override
    public void deleteCustomer(Long customerId) {
        customerRepository.deleteById(customerId);
    }

    @Override
    public CurrentBankAccountDTO saveCurrentBankAccount(double initialBalance, double overDraft, Long customerId) throws CustomerNotFoundException {
        Customer customer=customerRepository.findById(customerId).orElse(null);
        if(customer==null)
           throw new CustomerNotFoundException("Customer not found");
        CurrentAccount currentAccount = new CurrentAccount();
        currentAccount.setId(UUID.randomUUID().toString());
        currentAccount.setBalance(initialBalance);
        currentAccount.setCreatedAt(new Date());
        currentAccount.setOverDraft(overDraft);
        currentAccount.setCustomer(customer);
        currentAccount.setStatus(AccountStatus.CREATED);
        CurrentAccount savedBankAccount=bankAccountRepository.save(currentAccount);

        return bankAccountMapper.fromCurrentBankAccount(savedBankAccount);
    }

    @Override
    public SavingBankAccountDTO saveSavingBankAccount(double initialBalance, double interestRate, Long customerId) throws CustomerNotFoundException {
        Customer customer=customerRepository.findById(customerId).orElse(null);
        if(customer==null)
            throw new CustomerNotFoundException("Customer not found");
        SavingAccount savingAccount = new SavingAccount();
        savingAccount.setId(UUID.randomUUID().toString());
        savingAccount.setBalance(initialBalance);
        savingAccount.setCreatedAt(new Date());
        savingAccount.setInterestRate(interestRate);
        savingAccount.setCustomer(customer);
        savingAccount.setStatus(AccountStatus.CREATED);
        SavingAccount savedBankAccount=bankAccountRepository.save(savingAccount);
        return bankAccountMapper.fromSavingBankAccount(savingAccount);
    }


    @Override
    public List<CustomerDTO> listCustomers() {
        List<Customer> customers=customerRepository.findAll();
        List<CustomerDTO> customerDTOs= customers.stream().map(customer -> bankAccountMapper.fromCustomer(customer)).collect(Collectors.toList());
       /* ArrayList<CustomerDTO> customerDTOs=new ArrayList<>();
        for (Customer customer:customers){
            CustomerDTO customerDTO=new CustomerDTO();
            customerDTO.setId(customer.getId());
            customerDTO.setName(customer.getName());
            customerDTO.setEmail(customer.getEmail());
            customerDTOs.add(customerDTO);
        }*/
        return customerDTOs;
    }

    @Override
    public CustomerDTO getCustomer(Long customerId) throws CustomerNotFoundException {
        Customer customer=customerRepository.findById(customerId).orElseThrow(()->new CustomerNotFoundException("customer not found "));
        CustomerDTO customerDTO=bankAccountMapper.fromCustomer(customer);
        return customerDTO;
    }
    @Override
    public BankAccountDTO getBankAccount(String accountId) throws BankAccountNotFoundException {
        BankAccount bankAccount=bankAccountRepository.findById(accountId).orElseThrow(
                ()->new BankAccountNotFoundException("BankAccount Not Found")
        );
        if (bankAccount instanceof CurrentAccount){
            CurrentAccount currentAccount=(CurrentAccount) bankAccount;
            return bankAccountMapper.fromCurrentBankAccount(currentAccount);
        }else {
            SavingAccount savingAccount=(SavingAccount) bankAccount;
            return bankAccountMapper.fromSavingBankAccount(savingAccount);
        }
    }

    @Override
    public DebitDTO debit(String accountId, double amount, String description) throws BankAccountNotFoundException, BalanceNotSufficientException {
        BankAccount bankAccount=bankAccountRepository.findById(accountId).orElseThrow(
                ()->new BankAccountNotFoundException("BankAccount Not Found")
        );
         if(bankAccount.getBalance()<amount)
             throw new BalanceNotSufficientException("Balance Not Sufficient");
        AccountOperation accountOperation=new AccountOperation();
        accountOperation.setType(OperationType.DEBIT);
        accountOperation.setAmount(amount);
        accountOperation.setBankAccount(bankAccount);
        accountOperation.setOperationDate(new Date());
        accountOperation.setDescription(description);
        accountOperationRepository.save(accountOperation);
        bankAccount.setBalance(bankAccount.getBalance()-amount);
        bankAccountRepository.save(bankAccount);
        return null;
    }

    @Override
    public void credit(String accountId, double amount, String description) throws BankAccountNotFoundException {
        BankAccount bankAccount=bankAccountRepository.findById(accountId).orElseThrow(
                ()->new BankAccountNotFoundException("BankAccount Not Found")
        );
        AccountOperation accountOperation=new AccountOperation();
        accountOperation.setType(OperationType.CREDIT);
        accountOperation.setAmount(amount);
        accountOperation.setBankAccount(bankAccount);
        accountOperation.setOperationDate(new Date());
        accountOperation.setDescription(description);
        accountOperationRepository.save(accountOperation);
        bankAccount.setBalance(bankAccount.getBalance()+amount);
        bankAccountRepository.save(bankAccount);
    }

    @Override
    public void transfer(String accountIdSource, String accountIdDestination, double amount) throws BankAccountNotFoundException, BalanceNotSufficientException {
        debit(accountIdSource,amount,"Transfer To "+accountIdDestination);
        credit(accountIdDestination,amount,"Transfrer from "+accountIdSource);
    }

    @Override
    public List<BankAccountDTO> bankAccountList() {
        List<BankAccount> bankAccounts= bankAccountRepository.findAll();
        List<BankAccountDTO> bankAccountDTOS= bankAccounts.stream().map(bankAccount -> {
            if(bankAccount instanceof CurrentAccount){
                CurrentAccount currentAccount=(CurrentAccount) bankAccount;
                return  bankAccountMapper.fromCurrentBankAccount(currentAccount);
            }else {
                SavingAccount savingAccount=(SavingAccount) bankAccount;
                return  bankAccountMapper.fromSavingBankAccount(savingAccount);
            }
        }).collect(Collectors.toList());
        return bankAccountDTOS;
    }

    @Override
    public List<AccountOperationDTO> accountHistory(String accountId){
        List<AccountOperation> accountOperations =accountOperationRepository.findByBankAccountId(accountId);
        return accountOperations
                .stream()
                .map(accountOperation -> bankAccountMapper.fromAccountOperation(accountOperation))
                .collect(Collectors.toList());
    }

    @Override
    public AccountHistoryDTO getAccountHistory(String accountId, int page, int size) throws BankAccountNotFoundException {
        BankAccount bankAccount=bankAccountRepository.findById(accountId).orElse(null);
        if(bankAccount==null)throw  new BankAccountNotFoundException("BankAccount Not Found");
        Page<AccountOperation> accountOperations=accountOperationRepository.findByBankAccountIdOrderByOperationDateDesc(bankAccount.getId(), PageRequest.of(page,size));
        AccountHistoryDTO accountHistoryDTO=new AccountHistoryDTO();
       List<AccountOperationDTO> accountOperationDTOS= accountOperations
               .stream()
               .map(accountOperation -> bankAccountMapper.fromAccountOperation(accountOperation))
               .collect(Collectors.toList());
       accountHistoryDTO.setAccountOperationDTOS(accountOperationDTOS);
       accountHistoryDTO.setAccountId(bankAccount.getId());
       accountHistoryDTO.setBalance(bankAccount.getBalance());
       accountHistoryDTO.setCurrentPage(page);
       accountHistoryDTO.setPageSize(size);
       accountHistoryDTO.setTotalPages(accountOperations.getTotalPages());
       return accountHistoryDTO;
    }

    @Override
    public List<CustomerDTO> searchCustomers(String keyword) {
        List<Customer> customers=customerRepository.searchCustomer(keyword);
        return customers.stream().map(customer -> bankAccountMapper.fromCustomer(customer)).collect(Collectors.toList());
    }


    @Override
    public List<BankAccountDTO> BankAccountsByCustomerId(Long customerId){
        List<BankAccount> bankAccounts=bankAccountRepository.findByCustomer_Id(customerId);
        return bankAccounts.stream().map(bankAccount -> {
            if(bankAccount instanceof CurrentAccount){
                return bankAccountMapper.fromCurrentBankAccount((CurrentAccount) bankAccount);
            }else {
                return bankAccountMapper.fromSavingBankAccount((SavingAccount) bankAccount);
            }
        }).collect(Collectors.toList());
    }

    @Override
    public Map<String, Integer> getCustomerAccountCounts() {
        List<Customer> customers = customerRepository.findAll();
        return customers.stream()
                .collect(Collectors.toMap(
                        Customer::getName, // La clé sera le nom du client
                        customer -> customer.getBankAccounts().size() // La valeur sera le nombre de comptes
                ));
    }
}
