package com.cmmint.semantic_analyzer;

public class Quadruple {
    private String first;
    private String second;
    private String third;
    private String forth;

    public Quadruple(String first, String second, String third, String forth) {
        this.first = first;
        this.second = second;
        this.third = third;
        this.forth = forth;
    }

    public String getFirst() {
        return this.first;
    }

    public String getSecond() {
        return this.second;
    }

    public String getThird() {
        return third;
    }

    public String getForth() {
        return forth;
    }

    public void setFirst(String first) {
        this.first = first;
    }

    public void setSecond(String second) {
        this.second = second;
    }

    public void setThird(String third) {
        this.third = third;
    }

    public void setForth(String forth) {
        this.forth = forth;
    }

    @Override
    public String toString() {
        return String.format("(%s, %s, %s, %s)", first, second, third, forth);
    }
}
