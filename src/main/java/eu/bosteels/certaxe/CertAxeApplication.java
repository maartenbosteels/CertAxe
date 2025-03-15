package eu.bosteels.certaxe;

import eu.bosteels.certaxe.ct.Entry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

@SpringBootApplication
public class CertAxeApplication {

  public static void main(String[] args) {
    SpringApplication.run(CertAxeApplication.class, args);
  }


  @Bean
  public BlockingQueue<Entry> queue() {
    return new LinkedBlockingDeque<>(200);
  }
}
