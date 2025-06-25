package com.expense.tracker.service;

import com.expense.tracker.dto.RegistrationRequestDTO;
import com.expense.tracker.model.User;
import com.expense.tracker.repository.UserRepo;
import jakarta.persistence.EntityExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepo userRepo;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public void register(RegistrationRequestDTO requestDTO) {
        User user = toUser(requestDTO);
        if (userRepo.existsById(user.getUsername())) {
            throw new EntityExistsException();
        }
        user.setPassword(encoder.encode(user.getPassword()));
        userRepo.save(user);
    }

    private User toUser(RegistrationRequestDTO dto) {
        return new User(dto.username(), dto.password(), dto.role());
    }
}
