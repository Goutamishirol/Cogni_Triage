package com.precisioncare.cognitriage.risk;


public record RiskFactor(
        String name,
        String observedValue,
        double contribution,
        String rationale
){

    public boolean isProtective(){
        return contribution<0;
    }

}
