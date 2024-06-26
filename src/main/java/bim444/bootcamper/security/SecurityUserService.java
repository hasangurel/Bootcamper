package bim444.bootcamper.security;

import bim444.bootcamper.user.User;
import bim444.bootcamper.user.UserService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
@Service
public class SecurityUserService implements UserDetailsService {
    private final UserService userService;

    public SecurityUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userService.findByEmail(username);
        return new SecurityUser(user);
    }
}