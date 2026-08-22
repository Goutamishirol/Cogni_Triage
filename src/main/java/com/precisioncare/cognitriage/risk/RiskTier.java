package com.precisioncare.cognitriage.risk;



public enum RiskTier {
    HIGH("Prioritize and escalation to the next stage of diagonosis"),
    MEDIUM("has to be escalated but a few more tests have to be done alongside"),
    LOW("Safe as of now, Routine monitoring required");


private final String recommendation;

RiskTier(String recommendation){
    this.recommendation=recommendation;
}
public String getRecommendation(){
    return recommendation;
        }
}
