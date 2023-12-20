package com.arasholding.jetizzkuryeapp.apimodels;

import java.util.List;

public class ApiResponseModelObj<T> {
    public List<ErrorMessage> Errors;
    public String Status;
    public String Message;
    public T Result;
}
