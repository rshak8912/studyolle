package clone.study.studyolle.account;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class AccountController {

    private final SignUpFormValidator signUpFormValidator;
    private final AccountService accountService;
    private final AccountRepository accountRepository;


    @InitBinder("signUpForm")
    public void initBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(signUpFormValidator);
    }

    @GetMapping("/sign-up")
    public String signUpForm(Model model) {

        model.addAttribute(new SignUpForm());
        // == model.addAttribugte("signUpForm", new SignUpForm());
        return "account/sign-up";
    }

    @PostMapping("/sign-up") // @ModelAttribute 복합객체를 받아올 때 사용하지만, parameter로 쓰일 때 생략 가능
    public String signUpSubmit(@Valid @ModelAttribute SignUpForm signUpForm, Errors errors) {
        if (errors.hasErrors()) {
            return "account/sign-up";
        }

        Account account = accountService.processNewAccount(signUpForm);
        accountService.login(account);
        return "redirect:/";
    }

    @GetMapping("/check-email-token")
    public String checkEmailToken(String token, String email, Model model) {
        Optional<Account> account = accountRepository.findByEmail(email);
        String view = "account/checked-email";

        if (!account.isPresent()) {
            model.addAttribute("error", "wrong.error");
            return view;
        }

        if (!account.get().isValidToken(token)) {
            model.addAttribute("error", "wrong.token");
            return view;
        }
        account.get().completeSignUp();
        accountService.login(account.get());

        model.addAttribute("numberOfUser", accountRepository.count());
        model.addAttribute("nickname", account.get().getNickname());
        return view;
    }


}
