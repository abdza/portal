package org.portalengine.portal.security;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.portalengine.portal.entities.User;
import org.portalengine.portal.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.ldap.support.LdapUtils;
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

    /* Read application.properties with the following function:
	 * String keyValue = env.getProperty(key);
	 */
	@Autowired
	private Environment env;

    @Override
    public Authentication authenticate(Authentication auth) 
      throws AuthenticationException {
        String username = auth.getName();
        String password = auth.getCredentials()
            .toString();

        User curuser = userService.getRepo().findByUsername(username).orElse(null);
        
        if (curuser!=null) {

            if(env.getProperty("spring.ldap.base")!=null){
                ldapTemplate.setIgnorePartialResultException(true);            

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

                    AndFilter filter = new AndFilter();
                    filter.and(new EqualsFilter("objectclass", "person"));
                    filter.and(new EqualsFilter("uid", username));

                    // LdapQuery query = LdapQueryBuilder.query().filter(filter);

                    System.out.println("password used:" + password);
                    // boolean authldap = ldapTemplate.authenticate(LdapUtils.emptyLdapName(),filter.toString(),userService.getPasswordEncoder().encode(password));
                    boolean authldap = ldapTemplate.authenticate(LdapUtils.emptyLdapName(),filter.toString(),password);

                    /* try {
                        ldapTemplate.authenticate(query, userService.getPasswordEncoder().encode(password));
                        System.out.println("got correct password");
                    } catch (final Exception e) {
                        System.out.println("password incorrect");
                        System.out.println(e.toString());
                    } */

                    System.out.println("right pass:" + String.valueOf(authldap));
                }

                System.out.println("list:" + fromldap.toString());
            }

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