package bai4;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Bút toán kép gồm nhiều dòng Nợ/Có.
 * Quy tắc: Tổng Nợ phải bằng Tổng Có, nếu không thì từ chối ghi sổ.
 */
public final class JournalEntry {

    private final String journalId;
    private final String idempotencyKey;
    private final Instant createdAt;
    private final String description;
    private final List<JournalLine> lines;
    private final AtomicBoolean reversed = new AtomicBoolean(false);

    public JournalEntry(String journalId, String idempotencyKey, Instant createdAt, String description, List<JournalLine> lines) {
        this.journalId = Objects.requireNonNull(journalId, "journalId không được null");
        this.idempotencyKey = idempotencyKey;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt không được null");
        this.description = description;
        this.lines = Collections.unmodifiableList(Objects.requireNonNull(lines, "lines không được null"));

        validateBalanced();
    }

    private void validateBalanced() {
        if (lines.isEmpty()) {
            throw new InvalidTransactionException("Bút toán phải có ít nhất một dòng");
        }

        BigDecimal totalDebit = lines.stream()
                .filter(l -> l.type() == EntryType.DEBIT)
                .map(JournalLine::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCredit = lines.stream()
                .filter(l -> l.type() == EntryType.CREDIT)
                .map(JournalLine::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalDebit.compareTo(totalCredit) != 0) {
            throw new InvalidTransactionException(
                    "Bút toán không cân: Tổng Nợ (" + totalDebit + ") != Tổng Có (" + totalCredit + ")"
            );
        }
    }

    public String journalId() {
        return journalId;
    }

    public String idempotencyKey() {
        return idempotencyKey;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public String description() {
        return description;
    }

    public List<JournalLine> lines() {
        return lines;
    }

    public boolean isReversed() {
        return reversed.get();
    }

    public boolean markReversed() {
        return reversed.compareAndSet(false, true);
    }
}

