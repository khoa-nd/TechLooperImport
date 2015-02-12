package com.techlooper.imports;

import com.techlooper.configuration.VietnamworksDatabaseConfiguration;
import com.techlooper.repository.VietnamworksUserRepository;
import com.techlooper.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;
import java.util.Optional;

/**
 * Created by NguyenDangKhoa on 2/9/15.
 */
public class VietnamworksUserImport {

  private static Logger LOGGER = LoggerFactory.getLogger(VietnamworksUserImport.class);

  private static VietnamworksUserRepository vietnamworksUserRepository;


  public static void main(String[] args) throws Throwable {
    ApplicationContext applicationContext = new AnnotationConfigApplicationContext(VietnamworksDatabaseConfiguration.class);
    vietnamworksUserRepository = applicationContext.getBean("vietnamworksUserRepository", VietnamworksUserRepository.class);
    String enrichUserAPI = applicationContext.getEnvironment().getProperty("githubUserProfileEnricher.techlooper.api.enrichUser");

    final int totalUsers = vietnamworksUserRepository.getTotalUser();
    final int pageSize = 10;
    final int numberOfPages = totalUsers % pageSize == 0 ? totalUsers / pageSize : totalUsers / pageSize + 1;
    int pageIndex = 0;

    while (pageIndex < numberOfPages) {
      List<Long> resumes = vietnamworksUserRepository.getResumeList(pageIndex * pageSize, pageSize);
      Optional<String> vietnamworksUsers = Utils.toJSON(vietnamworksUserRepository.getUsersByResumeId(resumes));

      if (vietnamworksUsers.isPresent()) {
        String jsonUsers = vietnamworksUsers.get().replaceAll("skills", "skill");
        int result = Utils.postAndGetStatus(enrichUserAPI, jsonUsers);
        if (result == 204) {
          LOGGER.info("Imported user in page #" + pageIndex + " successfully.");
        }
        else {
          LOGGER.info("Import user in page #" + pageIndex + " fail.");
        }
      }
      pageIndex++;
      Thread.sleep(2000);
    }
  }

}
