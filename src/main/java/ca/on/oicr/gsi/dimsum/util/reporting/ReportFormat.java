package ca.on.oicr.gsi.dimsum.util.reporting;

import org.springframework.http.MediaType;

public enum ReportFormat {
  // @formatter:off
  EXCEL(new MediaType("application", "vnd.ms-excel"), "xlsx"),
  CSV(new MediaType("text", "csv"), "csv"),
  TSV(new MediaType("text", "tab-separated-values"), "tsv");
  // @formatter:on

  private final MediaType mediaType;
  private final String extension;

  private ReportFormat(MediaType mediaType, String extension) {
    this.mediaType = mediaType;
    this.extension = extension;
  }

  public MediaType getMediaType() {
    return mediaType;
  }

  public String getExtension() {
    return extension;
  }
}
