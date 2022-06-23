package ca.on.oicr.gsi.dimsum.service.filtering;

import static java.util.Objects.requireNonNull;

import java.util.function.Predicate;

import ca.on.oicr.gsi.dimsum.data.Case;

public class CaseFilter {

    private final CaseFilterKey filter;
    private final String value;

    public CaseFilter(CaseFilterKey filter, String value) {
        requireNonNull(filter);
        requireNonNull(value);
        this.filter = filter;
        this.value = value;
    }

    public Predicate<Case> predicate() {
        return filter.create().apply(value);
    }

}
