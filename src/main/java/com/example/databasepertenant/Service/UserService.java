package com.example.databasepertenant.Service;

import com.example.databasepertenant.model.User;
import com.example.databasepertenant.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


import com.example.databasepertenant.model.Role;
import com.example.databasepertenant.model.User;
import com.example.databasepertenant.repository.RoleRepository;
import com.example.databasepertenant.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User createUser(User user, Set<String> roleNames) {
        // Найти роли по именам и добавить их пользователю
        Set<Role> roles = new HashSet<>();
        for (String roleName : roleNames) {
            roleRepository.findByName(roleName).ifPresent(roles::add);
        }
        user.setRoles(roles);

        return userRepository.save(user);
    }

    public User updateUser(User user) {
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
