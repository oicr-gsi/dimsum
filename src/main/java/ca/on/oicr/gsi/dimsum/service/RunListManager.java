package ca.on.oicr.gsi.dimsum.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ca.on.oicr.gsi.dimsum.data.Run;
import ca.on.oicr.gsi.dimsum.data.RunAndLibraries;
import ca.on.oicr.gsi.dimsum.data.Sample;


@Service
public class RunListManager {
  private static final Logger log = LoggerFactory.getLogger(RunListManager.class);

  private List<Run> runs = new ArrayList<>();

  public void update(Map<String, RunAndLibraries> data) {
    // add new runs to existing list of runs
    data.values().stream().forEach(x -> {
      log.debug("Creating run {}", x.getRun().getName());
      this.runs.add(x.getRun());
    });
  }

  public List<Run> getRuns() {
    return this.runs;
  }

  private Predicate<RunAndLibraries> needsQc = x -> {
    return x.getRun().getDataReviewDate() == null || anyNeedQc(x.getLibraryQualifications())
        || anyNeedQc(x.getFullDepthSequencings());
  };

  private static boolean anyNeedQc(Collection<Sample> libraries) {
    return libraries.stream().anyMatch(x -> x.getDataReviewDate() == null);
  }
}
