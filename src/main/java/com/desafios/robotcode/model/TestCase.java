package com.desafios.robotcode.model;

import jakarta.persistence.*;

@Entity
@Table(name = "test_cases")
public class TestCase {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "problema_id")
    private Problema problema;
    
    @Column(name = "input_data", columnDefinition = "TEXT")
    private String input;
    
    @Column(name = "expected_output", columnDefinition = "TEXT")
    private String expectedOutput;
    
    @Column(name = "test_order")
    private Integer testOrder;
    
    public TestCase() {}
    
    public TestCase(String input, String expectedOutput) {
        this.input = input;
        this.expectedOutput = expectedOutput;
    }
    
    // Getters y setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Problema getProblema() {
        return problema;
    }
    
    public void setProblema(Problema problema) {
        this.problema = problema;
    }
    
    public String getInput() {
        return input;
    }
    
    public void setInput(String input) {
        this.input = input;
    }
    
    public String getExpectedOutput() {
        return expectedOutput;
    }
    
    public void setExpectedOutput(String expectedOutput) {
        this.expectedOutput = expectedOutput;
    }
    
    public Integer getTestOrder() {
        return testOrder;
    }
    
    public void setTestOrder(Integer testOrder) {
        this.testOrder = testOrder;
    }
} 