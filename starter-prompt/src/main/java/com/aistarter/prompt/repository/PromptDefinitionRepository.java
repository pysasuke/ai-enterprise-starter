package com.aistarter.prompt.repository;

import com.aistarter.prompt.entity.PromptDefinition;
import com.aistarter.prompt.entity.PromptType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PromptDefinitionRepository extends JpaRepository<PromptDefinition, Long> {

    Optional<PromptDefinition> findByPromptKeyAndType(String promptKey, PromptType type);

    List<PromptDefinition> findAllByOrderByPromptKeyAscTypeAsc();
}
