package ca.on.oicr.gsi.dimsum.data.external;

import java.util.List;
import java.util.stream.Collectors;
import ca.on.oicr.gsi.cardea.data.Test;

public class ExternalTest {

  private final String name;
  private final String tissueType;
  private final String tissueOrigin;
  private final String timepoint;
  private final String groupId;
  private final String libraryDesignCode;
  private final String targetedSequencing;
  private final boolean extractionSkipped;
  private final boolean libraryPreparationSkipped;
  private final boolean libraryQualificationSkipped;
  private final List<ExternalSample> extractions;
  private final List<ExternalSample> libraryPreparations;
  private final List<ExternalSample> libraryQualifications;
  private final List<ExternalSample> fullDepthSequencings;

  public ExternalTest(Test from) {
    name = from.getName();
    tissueType = from.getTissueType();
    tissueOrigin = from.getTissueOrigin();
    timepoint = from.getTimepoint();
    groupId = from.getGroupId();
    libraryDesignCode = from.getLibraryDesignCode();
    targetedSequencing = from.getTargetedSequencing();
    extractionSkipped = from.isExtractionSkipped();
    libraryPreparationSkipped = from.isLibraryPreparationSkipped();
    libraryQualificationSkipped = from.isLibraryQualificationSkipped();
    extractions = from.getExtractions().stream().map(ExternalSample::new)
        .collect(Collectors.toUnmodifiableList());
    libraryPreparations = from.getLibraryPreparations().stream().map(ExternalSample::new)
        .collect(Collectors.toUnmodifiableList());
    libraryQualifications = from.getLibraryQualifications().stream().map(ExternalSample::new)
        .collect(Collectors.toUnmodifiableList());
    fullDepthSequencings = from.getFullDepthSequencings().stream().map(ExternalSample::new)
        .collect(Collectors.toUnmodifiableList());
  }

  public String getName() {
    return name;
  }

  public String getTissueType() {
    return tissueType;
  }

  public String getTissueOrigin() {
    return tissueOrigin;
  }

  public String getTimepoint() {
    return timepoint;
  }

  public String getGroupId() {
    return groupId;
  }

  public String getLibraryDesignCode() {
    return libraryDesignCode;
  }

  public String getTargetedSequencing() {
    return targetedSequencing;
  }

  public boolean isExtractionSkipped() {
    return extractionSkipped;
  }

  public boolean isLibraryPreparationSkipped() {
    return libraryPreparationSkipped;
  }

  public boolean isLibraryQualificationSkipped() {
    return libraryQualificationSkipped;
  }

  public List<ExternalSample> getExtractions() {
    return extractions;
  }

  public List<ExternalSample> getLibraryPreparations() {
    return libraryPreparations;
  }

  public List<ExternalSample> getLibraryQualifications() {
    return libraryQualifications;
  }

  public List<ExternalSample> getFullDepthSequencings() {
    return fullDepthSequencings;
  }

}
