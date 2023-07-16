package com.studywa.studywa.account;

import com.studywa.studywa.domain.Account;
import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;

public class UserAccount extends User {

    @Getter
    private Account account;

    public UserAccount(Account account) {
        super(account.getNickname(), account.getPassword(), List.of(new SimpleGrantedAuthority("ROLE_USER")));
        this.account = account;

        Stream.of(Integer.valueOf(1))
                .collect(groupingBy())
    }
}
