package com.template.api.feature.item;

import java.time.Instant;
import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import org.hibernate.annotations.SQLRestriction;

/**
 * Sample catalog row. Soft-deleted when {@code deleted_at} is set; Hibernate filter
 * hides those rows from normal repository queries ({@link SQLRestriction}).
 */
@Entity
@Table(name = "items")
@SQLRestriction("deleted_at IS NULL")
public class Item {

	@Id
	private UUID id;

	/** Uniqueness among active rows is enforced by Flyway partial index (not JPA unique). */
	@Column(nullable = false, length = 120)
	private String name;

	@Column(length = 500)
	private String description;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@Column(name = "deleted_at")
	private Instant deletedAt;

	protected Item() {
	}

	public Item(String name, String description) {
		this.id = UUID.randomUUID();
		this.name = name;
		this.description = description;
	}

	@PrePersist
	void onCreate() {
		if (this.id == null) {
			this.id = UUID.randomUUID();
		}
		Instant now = Instant.now();
		this.createdAt = now;
		this.updatedAt = now;
	}

	void setId(UUID id) {
		this.id = id;
	}

	@PreUpdate
	void onUpdate() {
		this.updatedAt = Instant.now();
	}

	/** Soft-delete: row stays for audit; filtered out of normal queries. */
	void softDelete() {
		this.deletedAt = Instant.now();
	}

	public boolean isDeleted() {
		return deletedAt != null;
	}

	public UUID getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public Instant getDeletedAt() {
		return deletedAt;
	}
}
