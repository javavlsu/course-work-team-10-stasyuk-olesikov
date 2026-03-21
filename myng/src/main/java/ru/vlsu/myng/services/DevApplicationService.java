package ru.vlsu.myng.services;

import lombok.RequiredArgsConstructor;
import ru.vlsu.myng.entities.DevApplication;
import ru.vlsu.myng.repositories.DevApplicationRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DevApplicationService {
    private final DevApplicationRepository repo;

    public DevApplication save(DevApplication app) { return repo.save(app); }
}
