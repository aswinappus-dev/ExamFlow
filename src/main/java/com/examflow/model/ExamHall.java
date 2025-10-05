package com.examflow.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "examhall")
public class ExamHall {

    @Id
    private String examhallNo;
    private int capacity;
    private String blockno;

    // --- GETTERS AND SETTERS ---

    public String getExamhallNo() {
        return examhallNo;
    }

    public void setExamhallNo(String examhallNo) {
        this.examhallNo = examhallNo;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getBlockno() {
        return blockno;
    }

    public void setBlockno(String blockno) {
        this.blockno = blockno;
    }
}

