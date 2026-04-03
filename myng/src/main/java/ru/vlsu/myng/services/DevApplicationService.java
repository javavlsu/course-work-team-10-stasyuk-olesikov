package ru.vlsu.myng.services;

import lombok.RequiredArgsConstructor;
import ru.vlsu.myng.entities.DevApplication;
import ru.vlsu.myng.entities.User;
import ru.vlsu.myng.repositories.DevApplicationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DevApplicationService {
    private final DevApplicationRepository repo;

    public DevApplication save(DevApplication app) { return repo.save(app); }

    public List<DevApplication> findByUser(User u) { return repo.findByUser(u); }
}
