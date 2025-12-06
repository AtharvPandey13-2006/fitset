package com.fitset.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisResult {
    private String date; // ISO date string
    private String summary; // concise natural-language summary
    private Integer usefulnessScore; // 0-100
    private String usefulnessReason; // why this score
    private List<String> expectedResults; // e.g., expected DOMS areas, performance adaptations
    private List<String> positives; // what went well
    private List<String> improvements; // what to improve next time
    private List<String> nextSteps; // suggested next actions for tomorrow
}
