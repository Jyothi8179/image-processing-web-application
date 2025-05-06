package com.app.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class UserData {


    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "ip", unique = true)
    String ip;

    @Column(name = "count")
    int count;
}
