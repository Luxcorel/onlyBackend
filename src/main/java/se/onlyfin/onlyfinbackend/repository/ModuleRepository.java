package se.onlyfin.onlyfinbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.onlyfin.onlyfinbackend.model.dashboard_entity.ModuleEntity;

import java.time.Instant;
import java.util.List;

public interface ModuleRepository extends JpaRepository<ModuleEntity, Integer> {

    List<ModuleEntity> findAllByPostDateAfter(Instant postDate);

}
