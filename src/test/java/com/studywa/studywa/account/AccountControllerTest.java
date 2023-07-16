package com.studywa.studywa.account;

import com.studywa.studywa.domain.Account;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @MockBean
    JavaMailSender javaMailSender;

    @Test
    void 회원_가입_화면이_보인다() throws Exception {
        mockMvc.perform(get("/sign-up"))
                .andExpect(status().isOk())
                .andExpect(view().name("account/sign-up"))
                .andExpect(model().attributeExists("signUpForm"))
                .andExpect(unauthenticated());
    }

    @Test
    void 회원_가입_처리_입력값_오류() throws Exception {
        mockMvc.perform(post("/sign-up")
                        .param("nickname", "gmelon")
                        .param("email", "email..")
                        .param("password", "12345")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("account/sign-up"))
                .andExpect(unauthenticated());
    }

    @Test
    void 회원_가입_처리_입력값_정상() throws Exception {
        mockMvc.perform(post("/sign-up")
                        .param("nickname", "gmelon")
                        .param("email", "hsh1769@naver.com")
                        .param("password", "12345678")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/"))
                .andExpect(authenticated().withUsername("gmelon"));

        Account account = accountRepository.findByEmail("hsh1769@naver.com");
        assertThat(account).isNotNull();
        assertThat(account.getPassword()).isNotEqualTo("12345678");
        assertThat(account.getEmailCheckToken()).isNotNull();

        assertThat(accountRepository.existsByEmail("hsh1769@naver.com")).isTrue();
        // when 절 수행 이후 SimpleMailMessage 타입으로 send() 가 호출되었는지 검증
        then(javaMailSender).should().send(any(SimpleMailMessage.class));
    }

    @Test
    void 인증_메일_확인_입력값_오류() throws Exception {
        mockMvc.perform(get("/check-email-token")
                        .param("token", "aksdjlasdjlkasd")
                        .param("email", "email@email.com"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("error"))
                .andExpect(view().name("account/checked-email"))
                .andExpect(unauthenticated());
    }

    @Test
    void 인증_메일_확인_입력값_정상() throws Exception {
        Account account = Account.builder()
                .email("hsh1769@naver.com")
                .password("12345678")
                .nickname("gmelon")
                .build();
        Account newAccount = accountRepository.save(account);
        newAccount.generateEmailCheckToken();

        mockMvc.perform(get("/check-email-token")
                        .param("token", newAccount.getEmailCheckToken())
                        .param("email", newAccount.getEmail()))
                .andExpect(status().isOk())
                .andExpect(model().attributeDoesNotExist("error"))
                .andExpect(model().attributeExists("nickname"))
                .andExpect(model().attributeExists("numberOfUser"))
                .andExpect(view().name("account/checked-email"))
                .andExpect(authenticated().withUsername("gmelon"));
    }
}
