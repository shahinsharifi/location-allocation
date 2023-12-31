package de.wigeogis.pmedian.api;

import de.wigeogis.pmedian.database.dto.SessionDto;
import de.wigeogis.pmedian.database.service.SessionService;
import de.wigeogis.pmedian.job.JobManager;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@AllArgsConstructor
@RequestMapping("/api/v1")
public class LocationAllocationController {

  private final SessionService sessionService;
  private final JobManager optimizationJobManager;

  @RequestMapping(value = "/start", method = RequestMethod.POST)
  @ResponseBody
  public ResponseEntity<SessionDto> startOptimization(@RequestBody SessionDto sessionDto)
      throws Exception {

    SessionDto session = sessionService.createNewSession(sessionDto);
    optimizationJobManager.start(session);
    return ResponseEntity.ok().body(session);
  }

  @RequestMapping(value = "/abort", method = RequestMethod.POST)
  @ResponseBody
  public ResponseEntity<?> abortLocationAlgorithm(@RequestBody SessionDto sessionDto) {
    sessionDto = optimizationJobManager.stop(sessionDto);
    return ResponseEntity.ok().body(sessionDto);
  }

  @RequestMapping(value = "/resume", method = RequestMethod.POST)
  @ResponseBody
  public ResponseEntity<?> resumeLocationAlgorithm(@RequestBody SessionDto sessionDto) {

    return ResponseEntity.ok().body(sessionDto);
  }

  @RequestMapping(value = "/get_sessions", method = RequestMethod.POST)
  @ResponseBody
  public ResponseEntity<?> getSessions(@RequestBody List<String> sessionKeys) {
    return ResponseEntity.ok().body(sessionService.getAllById(sessionKeys));
  }
}
