package ma.laraki.ebankingbackend.web;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AllArgsConstructor;
import ma.laraki.ebankingbackend.dtos.CustomerDTO;
import ma.laraki.ebankingbackend.exceptions.CustomerNotFoundException;
import ma.laraki.ebankingbackend.services.BankAccountService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@SecurityRequirement(name = "BearerAuth")

@RestController
@AllArgsConstructor
@CrossOrigin("*")
@RequestMapping("/customers")
public class CustomerRestController {
    private BankAccountService bankAccountService;

    @GetMapping("")
    //@PreAuthorize("hasAuthority('SCOPE_USER')")
    public List<CustomerDTO> customers(){
        return bankAccountService.listCustomers();
    }
    @GetMapping("/search")
    //@PreAuthorize("hasAuthority('SCOPE_USER')")
    public List<CustomerDTO> searchCustomers(@RequestParam(name = "keyword",defaultValue = "") String keyword){
        return bankAccountService.searchCustomers("%"+keyword+"%");
    }
    @GetMapping("/{id}")
    //@PreAuthorize("hasAuthority('SCOPE_USER')")
    public CustomerDTO getCustomer(@PathVariable(name = "id") Long customerId) throws CustomerNotFoundException {
        return bankAccountService.getCustomer(customerId);
    }
    @PostMapping("")
    //@PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public CustomerDTO saveCustomer(@RequestBody CustomerDTO customerDTO){
        return bankAccountService.saveCustomer(customerDTO);
    }
    @PutMapping("/{id}")
    //@PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public CustomerDTO updateCustomer(@PathVariable(name = "id") Long customerId,@RequestBody CustomerDTO customerDTO){
        customerDTO.setId(customerId);
        return bankAccountService.updateCustomer(customerDTO);
    }
    @DeleteMapping("/{id}")
    //@PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public void deleteCustomer(@PathVariable(name = "id") Long customerId){
        bankAccountService.deleteCustomer(customerId) ;
    }

    @GetMapping("/account-counts") // Nouveau endpoint
    //@PreAuthorize("hasAuthority('SCOPE_ADMIN')") // ou SCOPE_ADMIN selon vos besoins
    public Map<String, Integer> getCustomerAccountCounts() {
        return bankAccountService.getCustomerAccountCounts();
    }
}
