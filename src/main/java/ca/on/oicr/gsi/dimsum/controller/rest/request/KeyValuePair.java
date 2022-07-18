package ca.on.oicr.gsi.dimsum.controller.rest.request;

public class KeyValuePair {
  private String key;
  private String value;

  public KeyValuePair(String key, String value) {
    this.key = key;
    this.value = value;
  }

  public String getKey() {
    return key;
  }

  public String getValue() {
    return value;
  }
}
