package org.pricelessfestival.crossoff.server.api;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.hibernate.Session;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

/**
 * Created by ivan on 5/12/18.
 */
@AllArgsConstructor
@Log4j2
public class TransactScanTicket extends CrossoffTransaction<ScanResult> {

    String code;
    boolean manualScan;

    @Override
    public ScanResult apply(Session session) {
        ScanResult result;
        Ticket ticket = session.bySimpleNaturalId(Ticket.class).load(code);
        if (ticket == null) {
            // code is not found in database
            log.warn("* UNKNOWN TICKET: {}", code);
            result = new ScanResult(false, "Invalid ticket code: " + code, null);
        } else if (ticket.getScanned() != null) {
            // ticket was already scanned
            String scannedAt = TimeUtil.formatTimestamp(ticket.getScanned(), ZoneId.systemDefault());
            String interval = TimeUtil.formatDuration(Duration.between(ticket.getScanned(), Instant.now()));
            log.warn("* DUPLICATE SCAN: {} (scanned {} ago, {})", ticket.getCode(), interval, scannedAt);
            result = new ScanResult(false, "Already scanned " + interval + " ago, " + scannedAt, ticket);
        } else if (ticket.getVoided() != null && ticket.getVoided()) {
            // ticket was voided!
            log.warn("* VOIDED TICKET SCAN: {}", ticket.getCode());
            result = new ScanResult(false, "Voided - ticket was cancelled/refunded!", ticket);
        } else {
            // successfully validated
            ticket.setScanned(Instant.now());
            if (manualScan) {
                ticket.setManualScan(true);
            }
            session.saveOrUpdate(ticket);
            log.info("* SCANNED VALID TICKET: {} {}", ticket.getCode(), ticket.getDescription());
            result = new ScanResult(true, "Valid Ticket", ticket);
        }
        return result;
    }
}
