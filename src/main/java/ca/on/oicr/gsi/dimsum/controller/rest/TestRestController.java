package ca.on.oicr.gsi.dimsum.controller.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/test")
public class TestRestController {

  @GetMapping
  public String test() {
    return "Hello world!";
  }

}
