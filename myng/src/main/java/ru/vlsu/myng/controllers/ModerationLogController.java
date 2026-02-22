package  ru.vlsu.myng.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ModerationLogController {

    @GetMapping("/moderation-log")
    public String moderationLogPage() {
        return "moderation_log";
    }
}