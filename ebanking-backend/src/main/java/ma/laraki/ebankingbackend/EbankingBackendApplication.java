package ma.laraki.ebankingbackend;

import ma.laraki.ebankingbackend.dtos.BankAccountDTO;
import ma.laraki.ebankingbackend.dtos.CurrentBankAccountDTO;
import ma.laraki.ebankingbackend.dtos.CustomerDTO;
import ma.laraki.ebankingbackend.dtos.SavingBankAccountDTO;
import ma.laraki.ebankingbackend.entities.*;
import ma.laraki.ebankingbackend.enums.AccountStatus;
import ma.laraki.ebankingbackend.enums.OperationType;
import ma.laraki.ebankingbackend.exceptions.BalanceNotSufficientException;
import ma.laraki.ebankingbackend.exceptions.BankAccountNotFoundException;
import ma.laraki.ebankingbackend.exceptions.CustomerNotFoundException;
import ma.laraki.ebankingbackend.repositories.*;
import ma.laraki.ebankingbackend.services.BankAccountService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@SpringBootApplication
public class EbankingBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(EbankingBackendApplication.class, args);
	}

	@Bean
	CommandLineRunner start(BankAccountService bankAccountService) {
		return args -> {
			Stream.of("Abdellah", "Kamal", "Yassine", "Omar", "Marouane", "Youssra").forEach(name -> {
				CustomerDTO customer = new CustomerDTO();
				customer.setName(name);
				customer.setEmail(name + "@gmail.com");
				bankAccountService.saveCustomer(customer);
			});
			bankAccountService.listCustomers().forEach(customer -> {
				try {
					bankAccountService.saveCurrentBankAccount(Math.random() * 90000, 9000, customer.getId());
					bankAccountService.saveSavingBankAccount(Math.random() * 90000, 5.5, customer.getId());
					List<BankAccountDTO> bankAccounts = bankAccountService.bankAccountList();
					for (BankAccountDTO bankAccount : bankAccounts) {
						String accountId;
						if (bankAccount instanceof SavingBankAccountDTO) {
							accountId = ((SavingBankAccountDTO) bankAccount).getId();
						} else {
							accountId = ((CurrentBankAccountDTO) bankAccount).getId();
						}
						for (int i = 0; i < 10; i++) {
							bankAccountService.credit(accountId, 10000 + Math.random() * 120000, "CREDIT");
							bankAccountService.debit(accountId, 1000 + Math.random() * 9000, "DEBIT");
						}
					}
				} catch (CustomerNotFoundException e) {
					e.printStackTrace();
				} catch (BankAccountNotFoundException e) {
					e.printStackTrace();
				} catch (BalanceNotSufficientException e) {
					e.printStackTrace();
				}
			});
		};
	}

	CommandLineRunner commandLineRunner(CustomerRepository customerRepository, BankAccountRepository bankAccountRepository, AccountOperationRepository accountOperationRepository) {
		return args -> {
			Stream.of("Omar", "Abdellah", "Marouane").forEach(name -> {
				Customer customer = new Customer();
				customer.setName(name);
				customer.setEmail(name + "@gmail.com");
				customerRepository.save(customer);
			});
			customerRepository.findAll().forEach(customer -> {
				CurrentAccount currentAccount = new CurrentAccount();
				currentAccount.setId(UUID.randomUUID().toString());
				currentAccount.setBalance(Math.random() * 90000);
				currentAccount.setCreatedAt(new Date());
				currentAccount.setStatus(AccountStatus.CREATED);
				currentAccount.setCustomer(customer);
				currentAccount.setOverDraft(9000);
				bankAccountRepository.save(currentAccount);
				SavingAccount savingAccount = new SavingAccount();
				savingAccount.setId(UUID.randomUUID().toString());
				savingAccount.setBalance(Math.random() * 90000);
				savingAccount.setCreatedAt(new Date());
				savingAccount.setStatus(AccountStatus.CREATED);
				savingAccount.setCustomer(customer);
				savingAccount.setInterestRate(5.5);
				bankAccountRepository.save(savingAccount);
			});

			bankAccountRepository.findAll().forEach(account -> {
				for (int i = 0; i < 5; i++) {
					AccountOperation accountOperation = new AccountOperation();
					accountOperation.setOperationDate(new Date());
					accountOperation.setBankAccount(account);
					accountOperation.setAmount(Math.random() * 12000);
					accountOperation.setType(Math.random() > 0.5 ? OperationType.DEBIT : OperationType.CREDIT);
					accountOperationRepository.save(accountOperation);
				}
			});


		};
	}

	@Bean
	CommandLineRunner createUsersAndRoles(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
		return args -> {
			// --- 1. Create Roles if they don't exist ---
			Role adminRole = roleRepository.findByRoleName("ADMIN").orElse(null);
			if (adminRole == null) {
				adminRole = new Role(null, "ADMIN");
				roleRepository.save(adminRole);
			}

			Role userRole = roleRepository.findByRoleName("USER").orElse(null);
			if (userRole == null) {
				userRole = new Role(null, "USER");
				roleRepository.save(userRole);
			}

			// --- 2. Create Users if they don't exist ---
			// User 'user1' with 'USER' role
			User user1 = userRepository.findByUsername("user1").orElse(null);
			if (user1 == null) {
				user1 = new User(null, "user1", passwordEncoder.encode("12345"), true, Arrays.asList(userRole));
				userRepository.save(user1);
				System.out.println("Created user: user1");
			}

			// User 'admin' with 'USER' and 'ADMIN' roles
			User adminUser = userRepository.findByUsername("admin").orElse(null);
			if (adminUser == null) {
				adminUser = new User(null, "admin", passwordEncoder.encode("12345"), true, Arrays.asList(userRole, adminRole));
				userRepository.save(adminUser);
				System.out.println("Created admin user: admin");
			}
		};
	}
}
