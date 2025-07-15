package ru.shift.userimporter.api.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class UserDTO {
    private String phone;
    private String name;
    private String lastName;
    private String middleName;
    private String email;
    private LocalDate birthdate;
    private LocalDateTime creationTime;
    private LocalDateTime updateTime;

    public UserDTO(String phone, String name, String lastName, String middleName, String email, LocalDate birthdate,
                   LocalDateTime creationTime, LocalDateTime updateTime) {
        this.phone = phone;
        this.name = name;
        this.lastName = lastName;
        this.middleName = middleName;
        this.email = email;
        this.birthdate = birthdate;
        this.creationTime = creationTime;
        this.updateTime = updateTime;
    }

    public String getPhone() { return phone; }
    public String getName() { return name; }
    public String getLastName() { return lastName; }
    public String getMiddleName() { return middleName; }
    public String getEmail() { return email; }
    public LocalDate getBirthdate() { return birthdate; }
    public LocalDateTime getCreationTime() { return creationTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setName(String name) { this.name = name; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setMiddleName(String middleName) { this.middleName = middleName; }
    public void setEmail(String email) { this.email = email; }
    public void setBirthdate(LocalDate birthdate) { this.birthdate = birthdate; }
    public void setCreationTime(LocalDateTime creationTime) { this.creationTime = creationTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}