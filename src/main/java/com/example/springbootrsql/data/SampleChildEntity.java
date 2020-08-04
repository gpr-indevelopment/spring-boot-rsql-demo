package com.example.springbootrsql.data;

import javax.persistence.*;

@Entity
public class SampleChildEntity {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    @ManyToOne
    @JoinColumn(name="parent_id")
    private SampleEntity parent;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SampleEntity getParent() {
        return parent;
    }

    public void setParent(SampleEntity parent) {
        this.parent = parent;
    }
}
