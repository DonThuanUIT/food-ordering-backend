package com.foodorderingapp.backend.security;

import com.foodorderingapp.backend.entity.User;
import com.foodorderingapp.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String phone) throws UsernameNotFoundException {

        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new UsernameNotFoundException("No account found with phone number: " + phone));

        return new CustomUserDetails(user);
    }
}