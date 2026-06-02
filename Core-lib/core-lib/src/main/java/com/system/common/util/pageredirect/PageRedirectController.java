package com.system.common.util.pageredirect;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/common/redirectPage")
@RequiredArgsConstructor
public class PageRedirectController {
    private final PageRedirectService pageRedirectService;

    @GetMapping("/redirect")
    @ResponseBody
    public String redirectToNewPage(@RequestParam("url") String param) throws Exception {
        if ("/menu/menuSettings.html".equals(param)) {
            param = "/menu/menuSettingsV2.html";
        }
        return pageRedirectService.pageLoad(param);
    }

}