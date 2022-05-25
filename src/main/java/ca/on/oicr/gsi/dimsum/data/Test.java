package ca.on.oicr.gsi.dimsum.data;

import java.util.List;

public class Test {

  private String name;
  private String tissueOrigin;
  private String tissueType;
  private String timepoint;
  private String groupId;
  private String targetedSequencing;
  private List<Sample> extractions;
  private List<Sample> libraryPreparations;
  private List<Sample> libraryQualifications;
  private List<Sample> fullDepthSequencings;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getTissueOrigin() {
    return tissueOrigin;
  }

  public void setTissueOrigin(String tissueOrigin) {
    this.tissueOrigin = tissueOrigin;
  }

  public String getTissueType() {
    return tissueType;
  }

  public void setTissueType(String tissueType) {
    this.tissueType = tissueType;
  }

  public String getTimepoint() {
    return timepoint;
  }

  public void setTimepoint(String timepoint) {
    this.timepoint = timepoint;
  }

  public String getGroupId() {
    return groupId;
  }

  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  public String getTargetedSequencing() {
    return targetedSequencing;
  }

  public void setTargetedSequencing(String targetedSequencing) {
    this.targetedSequencing = targetedSequencing;
  }

  public List<Sample> getExtractions() {
    return extractions;
  }

  public void setExtractions(List<Sample> extractions) {
    this.extractions = extractions;
  }

  public List<Sample> getLibraryPreparations() {
    return libraryPreparations;
  }

  public void setLibraryPreparations(List<Sample> libraryPreparations) {
    this.libraryPreparations = libraryPreparations;
  }

  public List<Sample> getLibraryQualifications() {
    return libraryQualifications;
  }

  public void setLibraryQualifications(List<Sample> libraryQualifications) {
    this.libraryQualifications = libraryQualifications;
  }

  public List<Sample> getFullDepthSequencings() {
    return fullDepthSequencings;
  }

  public void setFullDepthSequencings(List<Sample> fullDepthSequencings) {
    this.fullDepthSequencings = fullDepthSequencings;
  }
}
