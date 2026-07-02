package com.aistarter.prompt.entity;

import com.aistarter.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "prompt_definition", uniqueConstraints = @UniqueConstraint(columnNames = {"prompt_key", "type"}))
public class PromptDefinition extends BaseEntity {

    @Column(name = "prompt_key", nullable = false, length = 128)
    private String promptKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private PromptType type;

    @Column(length = 512)
    private String description;

    @Column(name = "active_version")
    private Integer activeVersion;
}
