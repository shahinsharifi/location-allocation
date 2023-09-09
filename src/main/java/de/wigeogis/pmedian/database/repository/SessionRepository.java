package de.wigeogis.pmedian.database.repository;

import de.wigeogis.pmedian.database.entity.Session;
import java.util.UUID;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface SessionRepository extends ListCrudRepository<Session, UUID> {}
