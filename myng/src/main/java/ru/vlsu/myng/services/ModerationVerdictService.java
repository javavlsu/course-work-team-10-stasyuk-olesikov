package ru.vlsu.myng.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.vlsu.myng.entities.DevApplication;
import ru.vlsu.myng.entities.ModerationVerdict;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ModerationVerdictService {
    private final ru.vlsu.myng.repositories.ModerationVerdictRepository repo;

    public ModerationVerdict save(ru.vlsu.myng.entities.ModerationVerdict verdict) { return repo.save(verdict); }

    public Optional<ModerationVerdict> findByDevApplication(DevApplication app) { return repo.findByDevApplication(app); }
}