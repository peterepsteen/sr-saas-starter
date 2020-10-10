package com.example.appname.service;

import com.example.appname.model.AppUser;
import com.example.appname.model.AppUserPrincipal;
import com.example.appname.repository.AppUserRepository;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.util.NoSuchElementException;

@Service
public class AppUserService implements UserDetailsService {
    @Autowired
    private AppUserRepository appUserRepository;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        val user = appUserRepository.findByEmail(email);
        if (user == null) {
            throw new NoSuchElementException("No user found with email " + email);
        }

        return new AppUserPrincipal(user);
    }

    public AppUser addUser(AppUser user) {
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        appUserRepository.save(user);
        return appUserRepository.findByEmail(user.getEmail());
    }
}
