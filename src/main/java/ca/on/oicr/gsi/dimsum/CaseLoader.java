package ca.on.oicr.gsi.dimsum;

import ca.on.oicr.gsi.dimsum.data.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class CaseLoader {

  private static final Logger log = LoggerFactory.getLogger(CaseLoader.class);

  private File projectFile;
  private File caseFile;
  private File donorFile;
  private File requisitionFile;
  private File sampleFile;
  private File timestampFile;

  private Timer refreshTimer = null;

  private ObjectMapper mapper = new ObjectMapper();

  public CaseLoader(@Value("${datadirectory}") File dataDirectory,
      @Autowired MeterRegistry meterRegistry) {
    if (!dataDirectory.isDirectory() || !dataDirectory.canRead()) {
      throw new IllegalStateException(
          String.format("Data directory unreadable: %s", dataDirectory.getAbsolutePath()));
    }
    if (meterRegistry != null) {
      refreshTimer = Timer.builder("case_data_refresh_time")
          .description("Time taken to refresh the case data").register(meterRegistry);
    }
    projectFile = new File(dataDirectory, "projects.json");
    caseFile = new File(dataDirectory, "cases.json");
    donorFile = new File(dataDirectory, "donors.json");
    requisitionFile = new File(dataDirectory, "requisitions.json");
    sampleFile = new File(dataDirectory, "samples.json");
    timestampFile = new File(dataDirectory, "timestamp");
  }

  /**
   * @return The time that the data finished writing, or null if the data is currently being written
   * @throws IOException if there is an error reading the timestamp file from disk
   */
  private ZonedDateTime getDataTimestamp() throws IOException {
    String timeString = Files.readString(timestampFile.toPath()).trim();
    if ("WORKING".equals(timeString)) {
      return null;
    }
    return ZonedDateTime.parse(timeString, DateTimeFormatter.ISO_DATE_TIME);
  }

  /**
   * Loads new case data if available
   *
   * @param previousTimestamp timestamp of previous successful load
   * @return case data if it is available and newer than the previousTimestamp; null otherwise
   * @throws DataParseException if there is an error parsing the data from file
   * @throws IOException if there is an error reading from disk
   */
  public CaseData load(ZonedDateTime previousTimestamp) throws DataParseException, IOException {
    log.debug("Loading case data...");
    ZonedDateTime beforeTimestamp = getDataTimestamp();
    if (beforeTimestamp == null) {
      log.debug("New case data is currently being written; aborting load.");
      return null;
    } else if (previousTimestamp != null && !beforeTimestamp.isAfter(previousTimestamp)) {
      log.debug("Current case data is up to date with data files; aborting load");
      return null;
    }
    long startTimeMillis = System.currentTimeMillis();
    try (FileReader projectReader = getProjectReader();
        FileReader sampleReader = getSampleReader();
        FileReader donorReader = getDonorReader();
        FileReader requisitionReader = getRequisitionReader();
        FileReader caseReader = getCaseReader();) {
      ZonedDateTime afterTimestamp = getDataTimestamp();
      if (afterTimestamp == null) {
        log.debug("New case data is currently being written; aborting load.");
        return null;
      } else if (!afterTimestamp.equals(beforeTimestamp)) {
        // Data was written in the middle of loading. Restart
        log.debug("New case data was written while we were reading; reloading...");
        return load(previousTimestamp);
      }
      Map<String, Project> projectsByName = loadProjects(projectReader);
      Map<String, Sample> samplesById = loadSamples(sampleReader);
      Map<String, Donor> donorsById = loadDonors(donorReader);
      Map<Long, Requisition> requisitionsById = loadRequisitions(requisitionReader);
      List<Case> cases =
          loadCases(caseReader, projectsByName, samplesById, donorsById, requisitionsById);
      Map<String, RunAndLibraries> runsByName = sortRuns(cases);
      if (refreshTimer != null) {
        refreshTimer.record(System.currentTimeMillis() - startTimeMillis, TimeUnit.MILLISECONDS);
      }
      log.debug(String.format("Completed loading %d cases.", cases.size()));
      return new CaseData(cases, runsByName, afterTimestamp, getAssayNames(cases),
          getRequisitionNames(requisitionsById), getProjectNames(projectsByName),
          getDonorNames(donorsById));
    }
  }

  private Set<String> getAssayNames(List<Case> cases) {
    Set<String> assayNames =
        new HashSet<String>(cases.stream().map(Case::getAssayName).collect(Collectors.toSet()));
    return assayNames;
  }

  private Set<String> getRequisitionNames(Map<Long, Requisition> requisitionsById) {
    Set<String> sampleNames = new HashSet<String>(
        requisitionsById.values().stream().map(Requisition::getName).collect(Collectors.toSet()));
    return sampleNames;
  }

  private Set<String> getProjectNames(Map<String, Project> projectsByName) {
    Set<String> projectNames =
        new HashSet<String>(projectsByName.keySet().stream().collect(Collectors.toSet()));
    return projectNames;
  }

  private Set<String> getDonorNames(Map<String, Donor> donorsById) {
    Set<String> donorNames = new HashSet<String>(
        donorsById.values().stream().map(Donor::getName).collect(Collectors.toSet()));
    return donorNames;
  }

  protected FileReader getProjectReader() throws FileNotFoundException {
    return new FileReader(projectFile);
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

  protected Map<String, Project> loadProjects(FileReader fileReader)
      throws DataParseException, IOException {
    List<Project> projects = loadFromJsonArrayFile(fileReader,
        json -> new Project.Builder().name(parseString(json, "name", true))
            .pipeline(parseString(json, "pipeline", true)).build());

    return projects.stream().collect(Collectors.toMap(Project::getName, Function.identity()));
  }

  protected Map<String, Sample> loadSamples(FileReader fileReader)
      throws DataParseException, IOException {
    List<Sample> samples = loadFromJsonArrayFile(fileReader,
        json -> new Sample.Builder().id(parseString(json, "sample_id", true))
            .name(parseString(json, "oicr_internal_name", true))
            .tissueOrigin(parseString(json, "tissue_origin", true))
            .tissueType(parseString(json, "tissue_type", true))
            .timepoint(parseString(json, "timepoint"))
            .groupId(parseString(json, "group_id"))
            .targetedSequencing(parseString(json, "targeted_sequencing"))
            .createdDate(parseSampleCreatedDate(json))
            .run(parseRun(json))
            .volume(parseDecimal(json, "volume", false))
            .concentration(parseDecimal(json, "concentration", false))
            .qcPassed(parseQcPassed(json, "qc_state"))
            .qcReason(parseString(json, "qc_reason"))
            .qcUser(parseString(json, "qc_user"))
            .qcDate(parseDate(json, "qc_date"))
            .dataReviewPassed(parseDataReviewPassed(json, "data_review_state"))
            .dataReviewUser(parseString(json, "data_review_user"))
            .dataReviewDate(parseDate(json, "data_review_date"))
            .build());

    return samples.stream().collect(Collectors.toMap(Sample::getId, Function.identity()));
  }

  private static LocalDate parseSampleCreatedDate(JsonNode json) throws DataParseException {
    LocalDate receivedDate = parseDate(json, "received");
    if (receivedDate != null) {
      return receivedDate;
    }
    LocalDate createdDate = parseDate(json, "created");
    if (createdDate != null) {
      return createdDate;
    }
    return parseDate(json, "entered");
  }

  private static Run parseRun(JsonNode json) throws DataParseException {
    String runName = parseString(json, "sequencing_run");
    return runName == null ? null : new Run.Builder().name(runName).build();
  }

  protected Map<String, Donor> loadDonors(FileReader fileReader)
      throws DataParseException, IOException {
    List<Donor> donors = loadFromJsonArrayFile(fileReader,
        json -> new Donor.Builder().id(parseString(json, "id", true))
            .name(parseString(json, "name", true))
            .externalName(parseString(json, "external_name", true)).build());

    return donors.stream().collect(Collectors.toMap(Donor::getId, Function.identity()));
  }

  protected Map<Long, Requisition> loadRequisitions(FileReader fileReader)
      throws DataParseException, IOException {
    List<Requisition> requisitions = loadFromJsonArrayFile(fileReader, json -> {
      Requisition requisition = new Requisition.Builder().id(parseLong(json, "id", true))
          .name(parseString(json, "name", true)).stopped(parseBoolean(json, "stopped"))
          .informaticsReviews(parseRequisitionQcs(json, "informatics_reviews"))
          .draftReports(parseRequisitionQcs(json, "draft_reports"))
          .finalReports(parseRequisitionQcs(json, "final_reports")).build();
      return requisition;
    });

    return requisitions.stream().collect(Collectors.toMap(Requisition::getId, Function.identity()));
  }

  private List<Case> loadCases(FileReader fileReader, Map<String, Project> projectsByName,
      Map<String, Sample> samplesById, Map<String, Donor> donorsById,
      Map<Long, Requisition> requisitionsById) throws DataParseException, IOException {
    return loadFromJsonArrayFile(fileReader, json -> {
      String donorId = parseString(json, "donor_id", true);
      return new Case.Builder()
          .id(parseString(json, "id", true))
          .donor(donorsById.get(donorId))
          .projects(parseProjects(json, "project_names", projectsByName))
          .assayName(parseString(json, "assay_name", true))
          .assayDescription(parseString(json, "assay_description", true))
          .tissueOrigin(parseString(json, "tissue_origin", true))
          .tissueType(parseString(json, "tissue_type", true))
          .timepoint(parseString(json, "timepoint"))
          .stopped(parseBoolean(json, "stopped"))
          .receipts(parseIdsAndGet(json, "receipt_ids", JsonNode::asText, samplesById))
          .tests(parseTests(json, "assay_tests", samplesById))
          .requisitions(parseIdsAndGet(json, "requisition_ids", JsonNode::asLong, requisitionsById))
          .build();
    });
  }

  private Set<Project> parseProjects(JsonNode json, String fieldName,
      Map<String, Project> projectsByName) throws DataParseException {
    JsonNode projectsNode = json.get("project_names");
    if (projectsNode == null || !projectsNode.isArray() || projectsNode.isEmpty()) {
      throw new DataParseException("Case has no projects");
    }
    Set<Project> projects = new HashSet<>();
    for (JsonNode projectNode : projectsNode) {
      projects.add(projectsByName.get(projectNode.asText()));
    }
    return projects;
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
      tests.add(new Test.Builder()
          .name(parseString(testNode, "name", true))
          .tissueOrigin(parseString(testNode, "tissue_origin"))
          .tissueType(parseString(testNode, "tissue_type"))
          .timepoint(parseString(testNode, "timepoint"))
          .groupId(parseString(testNode, "group_id"))
          .targetedSequencing(parseString(testNode, "targeted_sequencing"))
          .extractionSkipped(parseBoolean(testNode, "extraction_skipped"))
          .libraryPreparationSkipped(parseBoolean(testNode, "library_preparation_skipped"))
          .extractions(parseIdsAndGet(testNode, "extraction_ids", JsonNode::asText, samplesById))
          .libraryPreparations(
              parseIdsAndGet(testNode, "library_preparation_ids", JsonNode::asText, samplesById))
          .libraryQualifications(
              parseIdsAndGet(testNode, "library_qualification_ids", JsonNode::asText, samplesById))
          .fullDepthSequencings(
              parseIdsAndGet(testNode, "full_depth_sequencing_ids", JsonNode::asText, samplesById))
          .build());
    }
    return tests;
  }

  private Map<String, RunAndLibraries> sortRuns(List<Case> cases) {
    Map<String, RunAndLibraries.Builder> map = new HashMap<>();
    for (Case kase : cases) {
      for (Test test : kase.getTests()) {
        for (Sample sample : test.getLibraryQualifications()) {
          if (sample.getRun() != null) {
            addRunLibrary(map, sample, RunAndLibraries.Builder::addLibraryQualification);
          }
        }
        for (Sample sample : test.getFullDepthSequencings()) {
          addRunLibrary(map, sample, RunAndLibraries.Builder::addFullDepthSequencing);
        }
      }
    }
    return map.values().stream()
        .map(RunAndLibraries.Builder::build)
        .collect(Collectors.toMap(x -> x.getRun().getName(), Function.identity()));
  }

  private void addRunLibrary(Map<String, RunAndLibraries.Builder> map, Sample sample,
      BiConsumer<RunAndLibraries.Builder, Sample> addSample) {
    String runName = sample.getRun().getName();
    if (!map.containsKey(runName)) {
      map.put(runName, new RunAndLibraries.Builder().run(sample.getRun()));
    }
    addSample.accept(map.get(runName), sample);
  }

  private static String parseString(JsonNode json, String fieldName) throws DataParseException {
    return parseString(json, fieldName, false);
  }

  private static String parseString(JsonNode json, String fieldName, boolean required)
      throws DataParseException {
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

  private static Long parseLong(JsonNode json, String fieldName, boolean required)
      throws DataParseException {
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

  private static BigDecimal parseDecimal(JsonNode json, String fieldName, boolean required)
      throws DataParseException {
    String stringValue = parseString(json, fieldName, required);
    return stringValue == null ? null : new BigDecimal(stringValue);
  }

  private static List<RequisitionQc> parseRequisitionQcs(JsonNode json, String fieldName)
      throws DataParseException {
    JsonNode arr = json.get(fieldName);
    if (arr == null || !arr.isArray()) {
      throw new DataParseException(String.format("%s is not an array", fieldName));
    }
    List<RequisitionQc> qcs = new ArrayList<>();
    for (JsonNode node : arr) {
      qcs.add(new RequisitionQc.Builder().qcPassed(parseQcPassed(node, "qc_state"))
          .qcUser(parseString(node, "qc_user")).qcDate(parseDate(node, "qc_date")).build());
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

  private static Boolean parseDataReviewPassed(JsonNode json, String fieldName)
      throws DataParseException {
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
    // Remove time portion if included. Result is original recorded date (unmodified by timezone)
    if (dateString.contains("T")) {
      dateString = dateString.split("T")[0];
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
