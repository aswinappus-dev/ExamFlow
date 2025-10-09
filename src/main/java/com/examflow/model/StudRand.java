package com.examflow.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "stud_rand")
public class StudRand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "registerno")
    private String registerNo;
    
    @Column(name = "examhallno")
    private String examhallNo;
    
    @Column(name = "blockno")
    private String blockno;

    // ADDED: The new seatNo field to match your database and logic
    @Column(name = "seatno")
    private Integer seatNo;

    // --- GETTERS AND SETTERS ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRegisterNo() { return registerNo; }
    public void setRegisterNo(String registerNo) { this.registerNo = registerNo; }
    public String getExamhallNo() { return examhallNo; }
    public void setExamhallNo(String examhallNo) { this.examhallNo = examhallNo; }
    public String getBlockno() { return blockno; }
    public void setBlockno(String blockno) { this.blockno = blockno; }
    
    // ADDED: New getter and setter for the seatNo field
    public Integer getSeatNo() { return seatNo; }
    public void setSeatNo(Integer seatNo) { this.seatNo = seatNo; }
}

