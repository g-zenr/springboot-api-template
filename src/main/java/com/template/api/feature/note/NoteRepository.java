package com.template.api.feature.note;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface NoteRepository extends JpaRepository<Note, UUID>, JpaSpecificationExecutor<Note> {

	List<Note> findByOwnerUsernameOrderByUpdatedAtDesc(String ownerUsername);

	Optional<Note> findByIdAndOwnerUsername(UUID id, String ownerUsername);
}
