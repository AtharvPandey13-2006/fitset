package com.fitset.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "pricing_plans")
public class PricingPlan {
    @Id
    private String id;
    
    private String name;
    private Integer price; // in INR per month
    private String period; // /month, /year
    private Boolean featured;
    private Boolean active;
    
    private List<String> features;
    private String description;
    private Integer sortOrder;
}
