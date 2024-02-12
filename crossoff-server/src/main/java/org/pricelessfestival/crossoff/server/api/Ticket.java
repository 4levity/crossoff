package org.pricelessfestival.crossoff.server.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.base.Strings;
import lombok.*;
import lombok.experimental.Accessors;
import org.hibernate.annotations.NaturalId;
import org.hibernate.envers.Audited;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.regex.Pattern;

/**
 * Created by ivan on 4/26/18.
 */
@Entity
@Audited
@Table(name = "tickets")
@NoArgsConstructor
@Accessors(chain = true)
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Ticket {

    private static final int DEFAULT_ENTITY_STRING_LENGTH = 255;

    @Id
    @GeneratedValue
    @Column(name = "id")
    @JsonIgnore
    @Getter
    @Setter(AccessLevel.PRIVATE)
    private Long id;

    @NaturalId(mutable = false)
    @Column(name = "code", nullable = false, unique = true)
    @Getter
    @Setter(AccessLevel.PRIVATE)
    private String code; // value of the barcode printed on the ticket and scanned

    @Column(name = "description", nullable = false)
    @Getter
    @Setter
    private String description; // description of the ticket i.e. "general admission pass"

    @Column(name = "ticketholder")
    @Getter
    @Setter
    private String ticketholder; // name of the ticketholder, or null if not known

    @Column(name = "tickettype", nullable = false)
    @Getter
    @Setter
    private TicketType ticketType; // physical, print at home, mobile, etc

    @Column(name = "scanned")
    @Getter
    @Setter
    private Instant scanned; // when ticket was scanned, or null if never scanned

    @Column(name = "manualscan")
    @Getter
    @Setter
    private Boolean manualScan; // true if ticket was reported marked as scanned "manually" (rather than by barcode), null otherwise

    @Column(name = "voided")
    @Getter
    @Setter
    private Boolean voided; // true if ticket was voided (e.g. refund issued, ticket canceled), null otherwise

    @Column(name = "notes")
    @Getter
    @Setter
    private String notes; // door notes for this ticket, or null

    public Ticket(String code, String description, String ticketholder, TicketType ticketType, String notes) {
        this.code = code;
        this.description = description;
        this.ticketholder = ticketholder;
        this.ticketType = ticketType;
        this.notes = notes;
    }

    public Ticket(String code, String description, String ticketholder, TicketType ticketType) {
        this(code, description, ticketholder, ticketType, null);
    }

    // acceptable barcodes: 1-64 chars long, subset of Code 39 printable characters (no / + or [space])
    private static Pattern VALID_TICKET_CODE = Pattern.compile("^[A-Z0-9\\-\\$\\%\\*]{1,64}$");

    public static boolean validTicketCode(String code) {
        return code != null && VALID_TICKET_CODE.matcher(code).matches();
    }

    public static boolean validDescription(String description) {
        return !Strings.isNullOrEmpty(description) && description.length() <= DEFAULT_ENTITY_STRING_LENGTH;
    }

    public static boolean validTicketholder(String description) {
        return description == null || (!description.isEmpty() && description.length() <= DEFAULT_ENTITY_STRING_LENGTH);
    }

    public static boolean validNotes(String notes) {
        return notes == null || (!notes.isEmpty() && notes.length() <= DEFAULT_ENTITY_STRING_LENGTH);
    }

    public enum TicketType {
        // CAUTION: By default, Hibernate serializes enums by integer value.
        // Therefore, if you remove items or change the order of items below, compatibility will be broken with existing databases.
        // Adding new types at the end is OK and will not break compatibility.
        UNSPECIFIED,    //0
        WILL_CALL,      //1
        PRINT_AT_HOME,  //2
        WALK_UP_SALE,   //3
        MOBILE,         //4
        PHYSICAL_MAILED //5
    }
}
