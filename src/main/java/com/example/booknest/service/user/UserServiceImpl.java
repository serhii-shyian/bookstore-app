package com.example.booknest.service.user;

import com.example.booknest.dto.user.UserRegistrationRequestDto;
import com.example.booknest.dto.user.UserResponseDto;
import com.example.booknest.exception.RegistrationException;
import com.example.booknest.mapper.UserMapper;
import com.example.booknest.model.Role;
import com.example.booknest.model.User;
import com.example.booknest.repository.role.RoleRepository;
import com.example.booknest.repository.user.UserRepository;
import com.example.booknest.service.shoppingcart.ShoppingCartService;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final ShoppingCartService shoppingCartService;

    @Override
    public UserResponseDto register(UserRegistrationRequestDto registrationDto)
            throws RegistrationException {
        if (userRepository.findByEmail(registrationDto.email()).isPresent()) {
            throw new RegistrationException(
                    String.format("User with email %s already exists",
                            registrationDto.email()));
        }

        User userFromDto = userMapper.toEntity(registrationDto);
        userFromDto.setPassword(passwordEncoder.encode(registrationDto.password()));
        userFromDto.setRoles(findByNameContaining(List.of(
                Role.RoleName.USER)));

        User savedUser = userRepository.save(userFromDto);
        shoppingCartService.createShoppingCart(userFromDto);

        return userMapper.toDto(savedUser);
    }

    private Set<Role> findByNameContaining(List<Role.RoleName> rolesList) {
        return new HashSet<>(roleRepository.findAllByNameContaining(rolesList));
    }
}
