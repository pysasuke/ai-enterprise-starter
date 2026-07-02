package com.aistarter.prompt.repository;

import com.aistarter.prompt.entity.PromptDefinition;
import com.aistarter.prompt.entity.PromptVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PromptVersionRepository extends JpaRepository<PromptVersion, Long> {

    List<PromptVersion> findByDefinitionOrderByVersionDesc(PromptDefinition definition);

    Optional<PromptVersion> findByDefinitionAndVersion(PromptDefinition definition, int version);

    @Query("SELECT COALESCE(MAX(v.version), 0) FROM PromptVersion v WHERE v.definition = :definition")
    int findMaxVersion(@Param("definition") PromptDefinition definition);
}
