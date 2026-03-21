package ru.vlsu.myng.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.vlsu.myng.entities.ModerationVerdict;

@Service
@RequiredArgsConstructor
public class ModerationVerdictService {
    private final ru.vlsu.myng.repositories.ModerationVerdictRepository repo;

    public ModerationVerdict save(ru.vlsu.myng.entities.ModerationVerdict verdict) { return repo.save(verdict); }
}