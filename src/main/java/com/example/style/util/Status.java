package com.example.style.util;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

public enum Status {

    @JsonProperty("created")
    created,
    @JsonProperty("processed")
    processed,
    @JsonProperty("approved")
    approved,
    @JsonProperty("rejected")
    rejected
    ;
}
