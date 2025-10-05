package com.examflow.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// @Entity tells Spring this class is a representation of a database table.
@Entity
// @Table links this class to the "student" table in your database.
@Table(name = "student")
public class Student {

    // @Id marks this field as the primary key for the table.
    @Id
    // @Column maps this Java field to the "register_no" column in the database.
    @Column(name = "register_no")
    private String registerNo;

    // We can add a method to extract the branch from the register number.
    public String getBranch() {
        if (this.registerNo != null && this.registerNo.length() >= 7) {
            // Extracts the branch code (e.g., "AD", "CS") from "CEC22AD001"
            return this.registerNo.substring(5, 7);
        }
        return "UNKNOWN";
    }

    // Standard Getters and Setters
    public String getRegisterNo() {
        return registerNo;
    }

    public void setRegisterNo(String registerNo) {
        this.registerNo = registerNo;
    }
}
