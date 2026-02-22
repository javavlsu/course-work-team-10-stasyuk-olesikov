package  ru.vlsu.myng.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RunningGameController {

    @GetMapping("/running-game")
    public String runningGamePage() {
        return "running_game";
    }
}