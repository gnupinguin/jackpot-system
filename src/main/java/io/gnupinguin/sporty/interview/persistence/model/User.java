package io.gnupinguin.sporty.interview.persistence.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("user")
public record User(@Id Long id, String name) {
}
