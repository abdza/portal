package org.portalengine.portal.security;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.portalengine.portal.entities.User;
import org.portalengine.portal.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;

@Component
public class PortalAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private LdapTemplate ldapTemplate;

    @Override
    public Authentication authenticate(Authentication auth) 
      throws AuthenticationException {
        String username = auth.getName();
        String password = auth.getCredentials()
            .toString();

        User curuser = userService.getRepo().findByUsername(username).orElse(null);
        
        if (curuser!=null) {

            System.out.println("Found user:" + curuser.getName());

            List<String> fromldap = ldapTemplate.search(
                query().where("uid").is(username),
                    new AttributesMapper<String>() {
                        public String mapFromAttributes(Attributes attrs)
                    throws NamingException {
                        return attrs.get("uid").get().toString();
                    }
            });

            if(fromldap.size()>0){
                System.out.println("Got ldap");
                boolean authldap = ldapTemplate.authenticate("dc=springframework,dc=org","(uid=" + username + ")",password);
                System.out.println("right pass:" + String.valueOf(authldap));
            }

            System.out.println("list:" + fromldap.toString());

            boolean passmatch = userService.getPasswordEncoder().matches(password, curuser.getPassword());
            if(!passmatch){
                throw new BadCredentialsException("External system authentication failed");
            }
            List<SimpleGrantedAuthority> authorities = new ArrayList<SimpleGrantedAuthority>();
            if(curuser.getIsAdmin()!=null && curuser.getIsAdmin()) {
                authorities.add(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN"));
            }
            return new UsernamePasswordAuthenticationToken(username, password, authorities);
        } else {
            throw new BadCredentialsException("External system authentication failed");
        }
    }

    @Override
    public boolean supports(Class<?> auth) {
        return auth.equals(UsernamePasswordAuthenticationToken.class);
    }
}