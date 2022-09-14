package ca.on.oicr.gsi.dimsum.controller.rest;

import static ca.on.oicr.gsi.dimsum.controller.mvc.MvcUtils.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ca.on.oicr.gsi.dimsum.controller.BadRequestException;
import ca.on.oicr.gsi.dimsum.controller.rest.request.DataQuery;
import ca.on.oicr.gsi.dimsum.data.Run;
import ca.on.oicr.gsi.dimsum.service.NotificationManager;
import ca.on.oicr.gsi.dimsum.service.filtering.RunSort;
import ca.on.oicr.gsi.dimsum.service.filtering.TableData;

@RestController
@RequestMapping("/rest/notifications")
public class NotificationRestController {

  @Autowired
  private NotificationManager notificationManager;

  @PostMapping
  public TableData<Run> query(@RequestBody DataQuery query) {
    validateDataQuery(query);
    RunSort sort = parseSort(query, RunSort::getByLabel);
    boolean descending = parseDescending(query);
    if (query.getBaseFilter() != null
        || (query.getFilters() != null && query.getFilters().size() > 0)) {
      throw new BadRequestException("Requested invalid filters");
    }
    return notificationManager.getNotifications(query.getPageSize(), query.getPageNumber(), sort,
        descending);
  }

}
