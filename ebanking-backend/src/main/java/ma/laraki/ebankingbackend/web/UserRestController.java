package ma.laraki.ebankingbackend.web;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AllArgsConstructor;
import ma.laraki.ebankingbackend.dtos.*;
import ma.laraki.ebankingbackend.entities.Role;
import ma.laraki.ebankingbackend.entities.User;
import ma.laraki.ebankingbackend.repositories.RoleRepository;
import ma.laraki.ebankingbackend.repositories.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@SecurityRequirement(name = "BearerAuth")
@RestController
@CrossOrigin("*")
@RequestMapping("/users")
public class UserRestController {
    private final PasswordEncoder passwordEncoder;
    private UserRepository userRepository;
    private RoleRepository roleRepository;
    public UserRestController(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository=userRepository;
        this.roleRepository=roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/profile")
    public UserProfileDTO profile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) return null;

        UserProfileDTO dto = new UserProfileDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEnabled(user.isEnabled());
        dto.setRoles(user.getRoles().stream()
                .map(role -> role.getRoleName())
                .collect(Collectors.toList()));
        return dto;
    }

    @GetMapping("")
    public List<UserDTO> users() {
        return userRepository.findAll()
                .stream()
                .map(UserDTO::new)
                .collect(Collectors.toList());
    }
    @PostMapping("")
    public UserDTO createUser(@RequestBody UserCreationDTO userCreationDTO) {

        User user = new User();
        user.setUsername(userCreationDTO.getUsername());

        // TODO: You should encode password using PasswordEncoder for security
        user.setPassword(passwordEncoder.encode(userCreationDTO.getPassword()));
        user.setEnabled(true);  // or false, depending on your logic

        // Convert role names to Role entities
        List<Role> roles = userCreationDTO.getRoles().stream()
                .map(roleName -> roleRepository.findByRoleName(roleName)
                        .orElseThrow(() -> new RuntimeException("Role not found: " + roleName)))
                .collect(Collectors.toList());

        user.setRoles(roles);

        User savedUser = userRepository.save(user);

        return new UserDTO(savedUser);
    }

    @PutMapping("/{id}/enabled")
    public ResponseEntity<UserDTO> toggleUserEnabled(@PathVariable Long id, @RequestParam(name = "enabled") boolean enabled) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = optionalUser.get();
        user.setEnabled(enabled);
        userRepository.save(user);
        return ResponseEntity.ok(new UserDTO(user));
    }

    @PutMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(@RequestBody PasswordChangeDTO passwordChangeDTO, Principal principal) {
        String username = principal.getName();
        User user = userRepository.findByUsername(username).orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("message", "User not found"));        }

        // Compare old password
        if (!passwordEncoder.matches(passwordChangeDTO.getOldPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Collections.singletonMap("message", "Old password is incorrect"));        }

        // Update with new password
        user.setPassword(passwordEncoder.encode(passwordChangeDTO.getNewPassword()));
        userRepository.save(user);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Password changed successfully");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long id) {
        Optional<User> optionalUser = userRepository.findById(id);

        if (!optionalUser.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("message", "User not found"));
        }

        userRepository.deleteById(id);

        return ResponseEntity.ok(Collections.singletonMap("message", "User deleted successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @RequestBody UserUpdateDTO userUpdateDTO) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (!optionalUser.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        User user = optionalUser.get();

        // Update username and enabled
        user.setUsername(userUpdateDTO.getUsername());
        user.setEnabled(userUpdateDTO.isEnabled());

        // Update roles by fetching Role entities by roleName (assumes you have RoleRepository)
        List<Role> roles = roleRepository.findByRoleNameIn(userUpdateDTO.getRoles());
        user.setRoles(roles);

        userRepository.save(user);

        return ResponseEntity.ok(new UserDTO(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUser(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(user -> ResponseEntity.ok(new UserDTO(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public List<UserDTO> searchUsers(@RequestParam("username") String username) {
        List<User> users = userRepository.findByUsernameContainingIgnoreCase(username);
        return users.stream().map(UserDTO::new).collect(Collectors.toList());
    }

}
