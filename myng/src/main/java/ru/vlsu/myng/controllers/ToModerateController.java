package  ru.vlsu.myng.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ToModerateController {

    @GetMapping("/to-moderate")
    public String toModeratePage() {
        return "to_moderate";
    }
}