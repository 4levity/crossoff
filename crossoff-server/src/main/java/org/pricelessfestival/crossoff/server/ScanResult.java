package org.pricelessfestival.crossoff.server;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Created by ivan on 4/28/18.
 */
@JsonAutoDetect
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ScanResult {
    private boolean accepted;
    private String message;
    private Ticket ticket;
}
