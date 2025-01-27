package ca.on.oicr.gsi.dimsum.data.external;

import java.util.List;
import java.util.stream.Collectors;
import ca.on.oicr.gsi.cardea.data.Sample;
import ca.on.oicr.gsi.cardea.data.Test;
import ca.on.oicr.gsi.dimsum.util.DataUtils;

public record ExternalTest(String name, String tissueType, String tissueOrigin, String timepoint,
        String groupId, String libraryDesignCode, String targetedSequencing,
        boolean extractionSkipped,
        boolean libraryPreparationSkipped, boolean libraryQualificationSkipped,
        List<ExternalSample> extractions, List<ExternalSample> libraryPreparations,
        List<ExternalSample> libraryQualifications, List<ExternalSample> fullDepthSequencings) {

    public ExternalTest(Test from) {
        this(from.getName(),
                from.getTissueType(),
                from.getTissueOrigin(),
                from.getTimepoint(),
                from.getGroupId(),
                from.getLibraryDesignCode(),
                from.getTargetedSequencing(),
                from.isExtractionSkipped(),
                from.isLibraryPreparationSkipped(),
                from.isLibraryQualificationSkipped(),
                from.getExtractions().stream()
                        .filter(DataUtils::isPassed)
                        .map(ExternalSample::new)
                        .collect(Collectors.toUnmodifiableList()),
                from.getLibraryPreparations().stream()
                        .filter(DataUtils::isPassed)
                        .map(ExternalSample::new)
                        .collect(Collectors.toUnmodifiableList()),
                from.getLibraryQualifications().stream()
                        .filter(ExternalTest::passedOrTopUpConfirmed)
                        .map(ExternalSample::new)
                        .collect(Collectors.toUnmodifiableList()),
                from.getFullDepthSequencings().stream()
                        .filter(ExternalTest::passedOrTopUpConfirmed)
                        .map(ExternalSample::new)
                        .collect(Collectors.toUnmodifiableList()));
    }

    private static boolean passedOrTopUpConfirmed(Sample sample) {
        return DataUtils.isPassed(sample) || (DataUtils.isTopUpRequired(sample)
                && Boolean.TRUE.equals(sample.getDataReviewPassed()));
    }

}
