package com.rms.funds.hodings.analyser.model;

import lombok.*;

@Builder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class IdName {

    private Long id;
    private String name;
}
