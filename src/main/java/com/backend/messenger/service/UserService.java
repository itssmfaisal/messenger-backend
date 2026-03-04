package com.backend.messenger.service;

import com.backend.messenger.model.Role;
import com.backend.messenger.model.User;
import com.backend.messenger.repository.RoleRepository;
import com.backend.messenger.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostConstruct
    public void initDefaultUser() {
        Optional<Role> r = roleRepository.findByName("ROLE_USER");
        if (!r.isPresent()) {
            roleRepository.save(new Role("ROLE_USER"));
        }
        if (userRepository.findByUsername("admin").isEmpty()) {
            User u = new User("admin", passwordEncoder.encode("admin"));
            u.getRoles().add(roleRepository.findByName("ROLE_USER").get());
            userRepository.save(u);
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), mapRolesToAuthorities(user.getRoles()));
    }

    private Collection<? extends GrantedAuthority> mapRolesToAuthorities(Set<Role> roles) {
        return roles.stream().map(r -> new SimpleGrantedAuthority(r.getName())).collect(Collectors.toList());
    }

    public User register(String username, String password) {
        User u = new User(username, passwordEncoder.encode(password));
        Role role = roleRepository.findByName("ROLE_USER").orElseGet(() -> roleRepository.save(new Role("ROLE_USER")));
        u.getRoles().add(role);
        return userRepository.save(u);
    }
}
