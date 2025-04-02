package com.image.processing.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@Table(name = "image")
public class Image {

    public Long getImageSize() {
        return imageSize;
    }

    public void setImageSize(Long imageSize) {
        this.imageSize = imageSize;
    }

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

    public String getResizedImageName() {
        return resizedImageName;
    }

    public void setResizedImageName(String resizedImageName) {
        this.resizedImageName = resizedImageName;
    }

    public Boolean getResizedStatus() {
        return resizedStatus;
    }

    public void setResizedStatus(Boolean resizedStatus) {
        this.resizedStatus = resizedStatus;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "imageSize")
    private Long imageSize;

    @Column(name = "resizedImageName")
    private String resizedImageName;

    @Column(name = "resizedStatus")
    private Boolean resizedStatus;

}
