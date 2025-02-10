package com.epam.spring.service.impl;

import com.epam.spring.dto.request.user.CredentialChangeRequestDTO;
import com.epam.spring.dto.request.user.UserCredentialsRequestDTO;
import com.epam.spring.dto.response.JwtAuthenticationResponse;
import com.epam.spring.dto.response.UserCredentialsResponseDTO;
import com.epam.spring.error.exception.IncorrectCredentialsException;
import com.epam.spring.error.exception.LoginAttemptException;
import com.epam.spring.error.exception.ResourceNotFoundException;
import com.epam.spring.model.Token;
import com.epam.spring.model.User;
import com.epam.spring.repository.TokenRepository;
import com.epam.spring.repository.UserRepository;
import com.epam.spring.service.JwtService;
import com.epam.spring.service.LoginAttemptService;
import com.epam.spring.service.MyUserPrincipal;
import com.epam.spring.util.TransactionContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final LoginAttemptService loginAttemptService;
    private final UserDetailsService userDetailsService;

    public UserCredentialsResponseDTO changeCredentials(CredentialChangeRequestDTO credentialChangeRequest) {
        String username = credentialChangeRequest.getUsername();
        String transactionId = TransactionContext.getTransactionId();
        log.info("Transaction ID: {}, Changing credentials for user: {}",
                transactionId, username);
        User user = findUserOrThrowException(username);
        checkPassword(credentialChangeRequest.getOldPassword(), user);
        user.setPassword(credentialChangeRequest.getNewPassword());
        userRepository.save(user);
        log.info("Transaction ID: {}, Successfully changed credentials for user: {}",
                transactionId, username);
        return new UserCredentialsResponseDTO(user.getUsername(), user.getPassword());
    }

    public JwtAuthenticationResponse login(UserCredentialsRequestDTO request) {
        String username = request.getUsername();
        if (loginAttemptService.isBlocked(username)) {
            throw new LoginAttemptException();
        }
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, request.getPassword()));
            MyUserPrincipal userPrincipal = (MyUserPrincipal) userDetailsService.loadUserByUsername(username);
            loginAttemptService.resetAttempts(username);
            String jwtToken = jwtService.generateToken(userPrincipal);
            revokeAllUserTokens(userPrincipal.getUser());
            saveToken(jwtToken, userPrincipal.getUser());
            return new JwtAuthenticationResponse(jwtToken);
        } catch (BadCredentialsException ex) {
            loginAttemptService.loginFailed(username);
            throw new IncorrectCredentialsException();
        }

        /*
        String username = userCredentialsRequest.getUsername();
        String transactionId = TransactionContext.getTransactionId();
        log.info("Transaction ID: {}, Logging in user: {}",
                transactionId, username);
        User user = findUserOrThrowException(userCredentialsRequest.getUsername());
        checkPassword(userCredentialsRequest.getPassword(), user);
         */
    }

    private void saveToken(String token, User user) {
        tokenRepository.save(Token.builder()
                .token(token)
                .expired(false)
                .revoked(false)
                .user(user)
                .build());
    }

    private void revokeAllUserTokens(User user) {
        var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
        if (validUserTokens.isEmpty())
            return;
        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validUserTokens);
    }

    public void activateProfile(String username) {
        User user = findUserOrThrowException(username);
        user.setActive(!user.isActive());
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public void authenticate(String username, String password) {
        User user = findUserOrThrowException(username);
        checkPassword(password, user);
    }

    private static void checkPassword(String password, User user) {
        if (!Objects.equals(user.getPassword(), password)) {
            log.info("Transaction ID: {}, Incorrect password, user: {}",
                    TransactionContext.generateTransactionId(), password);
            throw new IncorrectCredentialsException("Incorrect password");
        }
    }

    private User findUserOrThrowException(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(ResourceNotFoundException::new);
    }
}
