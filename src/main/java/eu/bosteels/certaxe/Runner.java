package eu.bosteels.certaxe;

import eu.bosteels.certaxe.ct.Driver;
import eu.bosteels.certaxe.ct.LogList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

@Service
public class Runner implements CommandLineRunner  {

  private final Driver driver;
  
  @SuppressWarnings("HttpUrlsUsage")
  private final LogList xenon = new LogList("xenon2018", "http://ct.googleapis.com/logs/xenon2018/");

  public Runner(Driver driver) {
    this.driver = driver;
  }

  @Override
  public void run(String... args) throws Exception {
    driver.start(xenon);
  }
}
