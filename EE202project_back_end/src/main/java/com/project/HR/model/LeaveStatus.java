package com.project.HR.model;

import jakarta.persistence.Id;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "hr_leave_status")
@Data   // 生成 getter/setter、equals、hashCode、toString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveStatus {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(nullable = false, unique = true, length = 30)
	private String code;

	@Column(nullable = false, length = 50)
	private String name;

	private String description;
	@Column(name = "is_active", nullable = false)
	private boolean active = true;
	@Column(name = "is_terminal", nullable = false)
	private boolean terminal = false;
	@Column(name = "sort_order", nullable = false)
	private Integer sortOrder = 10;
	
}
