package com.oocl.springbootemployee;

public class EmployeeAgeNotValidException extends RuntimeException{
    public EmployeeAgeNotValidException(){
        System.out.println("EmployeeAgeNotValidException");
    }
}
