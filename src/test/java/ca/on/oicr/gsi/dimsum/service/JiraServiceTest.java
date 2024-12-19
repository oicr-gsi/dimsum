package ca.on.oicr.gsi.dimsum.service;

import org.junit.jupiter.api.Test;

public class JiraServiceTest {

  @Test
  public void testCreate() {
    // Construct the JiraService to ensure that runtime dependencies are satisfied
    new JiraService("http://localhost/jira", "test", "test", null);
  }

}
