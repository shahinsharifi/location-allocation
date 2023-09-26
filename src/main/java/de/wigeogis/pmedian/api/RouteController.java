package de.wigeogis.pmedian.api;

import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequestMapping("/api/v1")
public class RouteController {

  @RequestMapping(value = "/{path:[^\\.]*}")
  public String redirect(@PathVariable String path) {
    log.info("incoming request: " + path + " redirecting to /");
    return "forward:/";
  }

}
