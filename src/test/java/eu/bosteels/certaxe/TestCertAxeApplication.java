package eu.bosteels.certaxe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;

@TestConfiguration(proxyBeanMethods = false)
public class TestCertAxeApplication {

  public static void main(String[] args) {
    SpringApplication.from(CertAxeApplication::main).with(TestCertAxeApplication.class).run(args);
  }

}
