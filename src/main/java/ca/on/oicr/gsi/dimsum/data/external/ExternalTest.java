package ca.on.oicr.gsi.dimsum.data.external;

import java.util.List;
import java.util.stream.Collectors;
import ca.on.oicr.gsi.cardea.data.Test;

public record ExternalTest(String name, String tissueType, String tissueOrigin, String timepoint,
    String groupId, String libraryDesignCode, String targetedSequencing, boolean extractionSkipped,
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
        from.getExtractions().stream().map(ExternalSample::new)
            .collect(Collectors.toUnmodifiableList()),
        from.getLibraryPreparations().stream().map(ExternalSample::new)
            .collect(Collectors.toUnmodifiableList()),
        from.getLibraryQualifications().stream().map(ExternalSample::new)
            .collect(Collectors.toUnmodifiableList()),
        from.getFullDepthSequencings().stream().map(ExternalSample::new)
            .collect(Collectors.toUnmodifiableList()));
  }

}
