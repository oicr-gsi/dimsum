package ca.on.oicr.gsi.dimsum;

import ca.on.oicr.gsi.dimsum.data.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class CaseLoader {

  private File caseFile;
  private File donorFile;
  private File requisitionFile;
  private File sampleFile;
  private File timestampFile;

  private ObjectMapper mapper = new ObjectMapper();

  public CaseLoader(@Value("${datadirectory}") File dataDirectory) {
    if (!dataDirectory.isDirectory() || !dataDirectory.canRead()) {
      throw new IllegalStateException(String.format("Data directory unreadable: %s", dataDirectory.getAbsolutePath()));
    }
    caseFile = new File(dataDirectory, "preprocessed_cases.json");
    donorFile = new File(dataDirectory, "preprocessed_donors.json");
    requisitionFile = new File(dataDirectory, "preprocessed_requisitions.json");
    sampleFile = new File(dataDirectory, "preprocessed_samples.json");
    timestampFile = new File(dataDirectory, "timestamp");
  }

  public LocalDateTime getDataTimestamp() throws IOException {
    String timeString = Files.readString(timestampFile.toPath()).trim();
    return LocalDateTime.parse(timeString);
  }

  public CaseData load() throws DataParseException, IOException {
    LocalDateTime beforeTimestamp = getDataTimestamp();
    try (
      FileReader sampleReader = getSampleReader();
      FileReader donorReader = getDonorReader();
      FileReader requisitionReader = getRequisitionReader();
      FileReader caseReader = getCaseReader();
    ) {
      LocalDateTime afterTimestamp = getDataTimestamp();
      if (afterTimestamp.equals(beforeTimestamp)) {
        Map<String, Sample> samplesById = loadSamples(sampleReader);
        Map<String, Donor> donorsById = loadDonors(donorReader);
        Map<Long, Requisition> requisitionsById = loadRequisitions(requisitionReader);
        List<Case> cases = loadCases(caseReader, samplesById, donorsById, requisitionsById);
        return new CaseData(cases, afterTimestamp);
      }
    }
    // Data was written in the middle of loading. Restart
    return load();
  }

  protected FileReader getSampleReader() throws FileNotFoundException {
    return new FileReader(sampleFile);
  }

  protected FileReader getDonorReader() throws FileNotFoundException {
    return new FileReader(donorFile);
  }

  protected FileReader getRequisitionReader() throws FileNotFoundException {
    return new FileReader(requisitionFile);
  }

  protected FileReader getCaseReader() throws FileNotFoundException {
    return new FileReader(caseFile);
  }

  protected Map<String, Sample> loadSamples(FileReader fileReader) throws DataParseException, IOException {
    List<Sample> samples = loadFromJsonArrayFile(fileReader, json -> {
      Sample sample = new Sample();
      sample.setId(parseString(json, "sample_id", true));
      sample.setName(parseString(json, "oicr_internal_name", true));
      sample.setTissueOrigin(parseString(json, "tissue_origin", true));
      sample.setTissueType(parseString(json, "tissue_type", true));
      sample.setTimepoint(parseString(json, "timepoint"));
      sample.setGroupId(parseString(json, "group_id"));
      sample.setTargetedSequencing(parseString(json, "targeted_sequencing"));
      sample.setQcPassed(parseQcPassed(json, "qc_state"));
      sample.setQcReason(parseString(json, "qc_reason"));
      sample.setQcUser(parseString(json, "qc_user"));
      sample.setQcDate(parseDate(json, "qc_date"));
      sample.setDataReviewPassed(parseDataReviewPassed(json, "data_review_state"));
      sample.setDataReviewUser(parseString(json, "data_review_user"));
      sample.setDataReviewDate(parseDate(json, "data_review_date"));
      return sample;
    });

    return samples.stream().collect(Collectors.toMap(Sample::getId, Function.identity()));
  }

  protected Map<String, Donor> loadDonors(FileReader fileReader) throws DataParseException, IOException {
    List<Donor> donors = loadFromJsonArrayFile(fileReader, json -> {
      Donor donor = new Donor();
      donor.setId(parseString(json, "id", true));
      donor.setName(parseString(json, "name", true));
      donor.setExternalName(parseString(json, "external_name", true));
      return donor;
    });

    return donors.stream().collect(Collectors.toMap(Donor::getId, Function.identity()));
  }

  protected Map<Long, Requisition> loadRequisitions(FileReader fileReader) throws DataParseException, IOException {
    List<Requisition> requisitions = loadFromJsonArrayFile(fileReader, json -> {
      Requisition requisition = new Requisition();
      requisition.setId(parseLong(json, "id", true));
      requisition.setName(parseString(json, "name", true));
      requisition.setStopped(parseBoolean(json, "stopped"));
      requisition.setInformationReviews(parseRequisitionQcs(json, "informatics_reviews"));
      requisition.setDraftReports(parseRequisitionQcs(json, "draft_reports"));
      requisition.setFinalReports(parseRequisitionQcs(json, "final_reports"));
      return requisition;
    });

    return requisitions.stream().collect(Collectors.toMap(Requisition::getId, Function.identity()));
  }

  private List<Case> loadCases(FileReader fileReader, Map<String, Sample> samplesById,
      Map<String, Donor> donorsById, Map<Long, Requisition> requisitionsById)
      throws DataParseException, IOException {
    return loadFromJsonArrayFile(fileReader, json -> {
      Case item = new Case();
      String donorId = parseString(json, "donor_id", true);
      item.setDonor(donorsById.get(donorId));
      if (item.getDonor() == null) {
        throw new DataParseException(String.format("Donor %s not found", donorId));
      }
      JsonNode projectsNode = json.get("project_names");
      if (projectsNode == null || !projectsNode.isArray() || projectsNode.isEmpty()) {
        throw new DataParseException("Case has no projects");
      }
      Set<Project> projects = new HashSet<>();
      for (JsonNode projectNode : projectsNode) {
        Project project = new Project();
        project.setName(projectNode.asText());
        projects.add(project);
      }
      item.setProjects(projects);
      item.setAssayName(parseString(json, "assay_name", true));
      item.setAssayDescription(parseString(json, "assay_description", true));
      item.setTissueOrigin(parseString(json, "tissue_origin", true));
      item.setTissueType(parseString(json, "tissue_type", true));
      item.setTimepoint(parseString(json, "timepoint"));
      item.setStopped(parseBoolean(json, "stopped"));
      item.setReceipts(parseIdsAndGet(json, "receipt_ids", JsonNode::asText, samplesById));
      item.setTests(parseTests(json, "assay_tests", samplesById));
      item.setRequisitions(parseIdsAndGet(json, "requisition_ids", JsonNode::asLong,
          requisitionsById));
      return item;
    });
  }

  private <I, T> List<T> parseIdsAndGet(JsonNode json, String idsFieldName,
      Function<JsonNode, I> convertField, Map<I, T> itemsById) throws DataParseException {
    JsonNode idsNode = json.get(idsFieldName);
    if (idsNode == null || !idsNode.isArray()) {
      throw new DataParseException(String.format("Field %s is not an array", idsFieldName));
    }
    List<T> items = new ArrayList<>();
    for (JsonNode idNode : idsNode) {
      I itemId = convertField.apply(idNode);
      T item = itemsById.get(itemId);
      if (item == null) {
        throw new DataParseException(String.format("Item %s not found", itemId));
      }
      items.add(item);
    }
    return items;
  }

  private List<Test> parseTests(JsonNode json, String fieldName, Map<String, Sample> samplesById)
      throws DataParseException {
    JsonNode testsNode = json.get(fieldName);
    if (testsNode == null || !testsNode.isArray()) {
      throw new DataParseException(String.format("Field %s is not an array", fieldName));
    }
    List<Test> tests = new ArrayList<>();
    for (JsonNode testNode : testsNode) {
      Test test = new Test();
      test.setName(parseString(testNode, "name", true));
      test.setTissueOrigin(parseString(testNode, "tissue_origin"));
      test.setTissueType(parseString(testNode, "tissue_type"));
      test.setTimepoint(parseString(testNode, "timepoint"));
      test.setGroupId(parseString(testNode, "group_id"));
      test.setTargetedSequencing(parseString(testNode, "targeted_sequencing"));
      test.setExtractions(
          parseIdsAndGet(testNode, "extraction_ids", JsonNode::asText, samplesById));
      test.setLibraryPreparations(
          parseIdsAndGet(testNode, "library_preparation_ids", JsonNode::asText, samplesById));
      test.setLibraryQualifications(
          parseIdsAndGet(testNode, "library_qualification_ids", JsonNode::asText, samplesById));
      test.setFullDepthSequencings(
          parseIdsAndGet(testNode, "full_depth_sequencing_ids", JsonNode::asText, samplesById));
      tests.add(test);
    }
    return tests;
  }

  private static String parseString(JsonNode json, String fieldName) throws DataParseException {
    return parseString(json, fieldName, false);
  }

  private static String parseString(JsonNode json, String fieldName, boolean required) throws DataParseException {
    JsonNode node = json.get(fieldName);
    if (node == null || node.isNull()) {
      if (required) {
        throw new DataParseException(makeMissingFieldMessage(fieldName));
      } else {
        return null;
      }
    }
    return node.asText();
  }

  private static Long parseLong(JsonNode json, String fieldName, boolean required) throws DataParseException {
    JsonNode node = json.get(fieldName);
    if (node == null || node.isNull()) {
      if (required) {
        throw new DataParseException(makeMissingFieldMessage(fieldName));
      } else {
        return null;
      }
    } else {
      return node.asLong();
    }
  }

  private static boolean parseBoolean(JsonNode json, String fieldName) throws DataParseException {
    JsonNode node = json.get(fieldName);
    if (node == null || node.isNull()) {
      throw new DataParseException(makeMissingFieldMessage(fieldName));
    } else {
      return node.asBoolean();
    }
  }

  private static List<RequisitionQc> parseRequisitionQcs(JsonNode json, String fieldName)
      throws DataParseException {
    JsonNode arr = json.get(fieldName);
    if (arr == null || !arr.isArray()) {
      throw new DataParseException(String.format("%s is not an array", fieldName));
    }
    List<RequisitionQc> qcs = new ArrayList<>();
    for (JsonNode node : arr) {
      RequisitionQc qc = new RequisitionQc();
      qc.setQcPassed(parseQcPassed(node, "qc_state"));
      qc.setQcUser(parseString(node, "qc_user"));
      qc.setQcDate(parseDate(node, "qc_date"));
      qcs.add(qc);
    }
    return qcs;
  }

  private static String makeMissingFieldMessage(String fieldName) {
    return String.format("Required value '%s' missing", fieldName);
  }

  private static Boolean parseQcPassed(JsonNode json, String fieldName) throws DataParseException {
    String qcState = parseString(json, fieldName, true);
    switch (qcState) {
      case "Ready":
        return Boolean.TRUE;
      case "Failed":
        return Boolean.FALSE;
      case "Not Ready":
        return null;
      default:
        throw new DataParseException(String.format("Unhandled QC state: %s", qcState));
    }
  }

  private static Boolean parseDataReviewPassed(JsonNode json, String fieldName) throws DataParseException {
    String qcState = parseString(json, fieldName);
    if (qcState == null) {
      return null;
    }
    switch (qcState) {
      case "Passed":
        return Boolean.TRUE;
      case "Failed":
        return Boolean.FALSE;
      case "Pending":
        return null;
      default:
        throw new DataParseException(String.format("Unhandled data review state: %s", qcState));
    }
  }

  private static LocalDate parseDate(JsonNode json, String fieldName) throws DataParseException {
    String dateString = parseString(json, fieldName);
    if (dateString == null) {
      return null;
    }
    try {
      return LocalDate.parse(dateString);
    } catch (DateTimeParseException e) {
      throw new DataParseException(String.format("Bad date format: %s", dateString));
    }
  }

  @FunctionalInterface
  private static interface ParseFunction<T, R> {
    R apply(T input) throws DataParseException;
  }

  private <T> List<T> loadFromJsonArrayFile(FileReader fileReader, ParseFunction<JsonNode, T> map)
      throws DataParseException, IOException {
    JsonNode json = mapper.readTree(fileReader);
    if (!json.isArray()) {
      throw new DataParseException("Top level JSON node is not an array");
    }
    List<T> loaded = new ArrayList<>();
    for (JsonNode node : json) {
      loaded.add(map.apply(node));
    }
    return loaded;
  }

}
