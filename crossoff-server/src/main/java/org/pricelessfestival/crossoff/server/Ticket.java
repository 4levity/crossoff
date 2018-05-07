package org.pricelessfestival.crossoff.server;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.hibernate.annotations.NaturalId;

import javax.persistence.*;
import java.time.Instant;
import java.util.regex.Pattern;

/**
 * Created by ivan on 4/26/18.
 */
@Entity
@Table(name = "tickets")
@NoArgsConstructor
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Ticket {

    @Id
    @GeneratedValue
    @Column(name = "id")
    @JsonIgnore
    @Getter
    @Setter(AccessLevel.PRIVATE)
    private Long id;

    @NaturalId
    @Column(name = "code", nullable = false, unique = true)
    @Getter
    @Setter
    private String code; // value of the barcode printed on the ticket and scanned

    @Column(name = "description", nullable = false)
    @Getter
    @Setter
    private String description;
    public static String _description = "description";

    @Column(name = "scanned")
    @Getter
    @Setter
    private Instant scanned; // when ticket was scanned, or null if never scanned

    public Ticket(String code, String description) {
        this.code = code;
        this.description = description;
    }

    private static Pattern VALID_TICKET_CODE = Pattern.compile("^[A-Z0-9\\-]+$");

    public static boolean validTicketCode(String code) {
        return code != null && VALID_TICKET_CODE.matcher(code).matches();
    }
}
