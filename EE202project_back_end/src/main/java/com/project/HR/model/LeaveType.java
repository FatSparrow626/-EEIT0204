package com.project.HR.model;
import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "hr_leave_type")
public class LeaveType implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name", length = 30, nullable = false, unique = true)
    private String name;

    @Column(name = "description", length = 200)
    private String description;

    public LeaveType() {}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
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

    
}
