package com.techlooper.imports;

import com.techlooper.configuration.VietnamworksDatabaseConfiguration;
import com.techlooper.pojo.VietnamworksUser;
import com.techlooper.repository.VietnamworksUserRepository;
import com.techlooper.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Optional;

/**
 * Created by NguyenDangKhoa on 2/9/15.
 */
public class VietnamworksUserImport {

    private static Logger LOGGER = LoggerFactory.getLogger(VietnamworksUserImport.class);

    private static VietnamworksUserRepository vietnamworksUserRepository;

    private static final int MAX_POST_SIZE = 2000000;

    public static void main(String[] args) throws Throwable {
        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(VietnamworksDatabaseConfiguration.class);
        vietnamworksUserRepository = applicationContext.getBean("vietnamworksUserRepository", VietnamworksUserRepository.class);
        String enrichUserAPI = applicationContext.getEnvironment().getProperty("githubUserProfileEnricher.techlooper.api.enrichUser");

        final int totalUsers = vietnamworksUserRepository.getTotalUser();
        final int pageSize = 1;
        final int numberOfPages = totalUsers % pageSize == 0 ? totalUsers / pageSize : totalUsers / pageSize + 1;
        int pageIndex = 0;

        while (pageIndex < numberOfPages) {
            List<Long> resumes = vietnamworksUserRepository.getResumeList(pageIndex * pageSize, pageSize);
            List<VietnamworksUser> vietnamworksUsers = vietnamworksUserRepository.getUsersByResumeId(resumes);

            if (vietnamworksUsers.isEmpty()) {
              LOGGER.info("User #" + pageIndex + " is inactive.");
            } else {
                String jsonUsers = processUserData(vietnamworksUsers);
                int result = Utils.postAndGetStatus(enrichUserAPI, jsonUsers);
                if (result == 204) {
                  LOGGER.info("Imported user in page #" + pageIndex + " successfully.");
                } else {
                  LOGGER.info("Import user in page #" + pageIndex + " fail. Error Code = " + result);
                }
            }
            pageIndex++;
            Thread.sleep(1000);
        }
    }

  private static String processUserData(List<VietnamworksUser> vietnamworksUsers) throws UnsupportedEncodingException {
    Optional<String> vietnamworksUsersJsonOpt = Utils.toJSON(vietnamworksUsers);

    if (vietnamworksUsersJsonOpt.isPresent()) {
      String jsonUsers = vietnamworksUsersJsonOpt.get();
      if (jsonUsers.getBytes("UTF-8").length > MAX_POST_SIZE) {
        for (VietnamworksUser user : vietnamworksUsers) {
          user.setResumecontent("");
        }
        jsonUsers = Utils.toJSON(vietnamworksUsers).get();
      }
      return jsonUsers.replaceAll("skills", "skill");
    }
    return null;
  }

}
