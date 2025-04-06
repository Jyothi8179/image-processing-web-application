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

    public String getOriginalFilePath() {
        return originalFilePath;
    }

    public void setOriginalFilePath(String originalFilePath) {
        this.originalFilePath = originalFilePath;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public String getResizedFilePath() {
        return resizedFilePath;
    }

    public void setResizedFilePath(String resizedFilePath) {
        this.resizedFilePath = resizedFilePath;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name="width")
    private Integer width;

    @Column(name="height")
    private Integer height;

    @Column(name = "imageSize")
    private Long imageSize;

    @Column(name = "resizedImageName")
    private String resizedImageName;

    @Column(name = "resizedStatus")
    private Boolean resizedStatus;

    @Column(name = "originalFilePath")
    private String originalFilePath;

    @Column(name = "resizedFilePath")
    private String resizedFilePath;

}
