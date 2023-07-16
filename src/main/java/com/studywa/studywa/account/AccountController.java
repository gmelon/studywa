package com.studywa.studywa.account;

import com.studywa.studywa.domain.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RequiredArgsConstructor
@Controller
public class AccountController {

    private final SignUpFormValidator signUpFormValidator;
    private final AccountService accountService;
    private final AccountRepository accountRepository;


    @InitBinder("signUpForm") // 검증하고 싶은 타입의 camelCase로 지정
    public void initBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(signUpFormValidator);
    }

    @GetMapping("/sign-up")
    public String signUpForm(Model model) {
        model.addAttribute(new SignUpForm());
        return "account/sign-up";
    }

    @PostMapping("/sign-up")
    public String signUpSubmit(@Valid @ModelAttribute SignUpForm form, Errors errors) {
        if (errors.hasErrors()) {
            return "account/sign-up";
        }

        Account newAccount = accountService.processNewAccount(form);
        accountService.login(newAccount);

        return "redirect:/";
    }

    @GetMapping("check-email-token")
    public String checkEmailToken(@RequestParam String token, @RequestParam String email, Model model) {
        Account account = accountRepository.findByEmail(email);
        String view = "account/checked-email";

        if (account == null) {
            model.addAttribute("error", "wrong.email");
            return view;
        }

        if (!account.isValidToken(token)) {
            model.addAttribute("error", "wrong.token");
            return view;
        }

        // 가입 완료 처리
        account.completeSignUp();

        // 로그인 처리
        accountService.login(account);

        model.addAttribute("numberOfUser", accountRepository.count());
        model.addAttribute("nickname", account.getNickname());
        return view;
    }

}
