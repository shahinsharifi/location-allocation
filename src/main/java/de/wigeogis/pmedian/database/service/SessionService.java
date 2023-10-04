package de.wigeogis.pmedian.database.service;

import de.wigeogis.pmedian.database.dto.SessionDto;
import de.wigeogis.pmedian.database.entity.Session;
import de.wigeogis.pmedian.database.entity.SessionStatus;
import de.wigeogis.pmedian.database.repository.SessionRepository;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class SessionService {

  private final ModelMapper modelMapper;
  private final SessionRepository repository;

  public SessionDto createNewSession(SessionDto sessionDto) {
    return entityToDto(repository.save(dtoToEntity(sessionDto)));
  }

  public SessionDto updateSessionStatus(UUID uuid, SessionStatus status) {
    SessionDto sessionDto = this.getById(uuid);
    sessionDto.setStatus(status);
    return entityToDto(repository.save(dtoToEntity(sessionDto)));
  }

  public SessionDto entityToDto(Session session) {
    return modelMapper.map(session, SessionDto.class);
  }

  public Session dtoToEntity(SessionDto sessionDto) {
    return modelMapper.map(sessionDto, Session.class);
  }

  public SessionDto getById(UUID uuid) {
    Session session = repository.findById(uuid).orElse(null);
    return entityToDto(session);
  }

  public List<Session> getAll() {
    return repository.findAll();
  }

  public List<Session> getAllById(List<String> sessionKeys) {
    return repository.findAllById(
        sessionKeys.stream()
            .map(s -> s.replaceAll("appState_", ""))
            .map(UUID::fromString)
            .collect(Collectors.toList()));
  }
}
