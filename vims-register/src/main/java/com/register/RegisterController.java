package com.register;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/reg")
public class RegisterController {

    @GetMapping
    public String index() {
        return "page/register/index";
    }

}
