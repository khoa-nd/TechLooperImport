package com.techlooper.service;

import com.techlooper.entity.UserImportEntity;
import com.techlooper.pojo.GithubAwardModel;
import com.techlooper.pojo.GithubAwardResponse;
import com.techlooper.pojo.SocialProvider;
import com.techlooper.pojo.UserImportLocationRank;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by NguyenDangKhoa on 3/27/15.
 */
@Service
public class GithubAwardDataProcessor {

    public void process(UserImportEntity userImportEntity, GithubAwardResponse githubAwardResponse) {
        Map<String, Object> profile = (Map<String, Object>) userImportEntity.getProfiles().get(SocialProvider.GITHUB);
        if (profile != null) {
            List<String> currentSkills = (List<String>) profile.get("skills");
            Map<String, List<UserImportLocationRank>> ranks = (Map<String, List<UserImportLocationRank>>) profile.get("ranks");

            if (ranks == null) {
                ranks = new HashMap<>();
            }

            List<GithubAwardModel> languageRanks = githubAwardResponse.getResults();
            for (GithubAwardModel languageRank : languageRanks) {
                processLanguageRank(languageRank);
                List<UserImportLocationRank.LocationRank> locations = new ArrayList<>();

                int[] rankTokens = processRankByLocation(languageRank.getCityRank());
                locations.add(new UserImportLocationRank.LocationRank("city", languageRank.getCity(), rankTokens[0], rankTokens[1]));
                rankTokens = processRankByLocation(languageRank.getCountryRank());
                locations.add(new UserImportLocationRank.LocationRank("country", languageRank.getCountry(), rankTokens[0], rankTokens[1]));
                rankTokens = processRankByLocation(languageRank.getWorldwideRank());
                locations.add(new UserImportLocationRank.LocationRank("worldwide", "worldwide", rankTokens[0], rankTokens[1]));

                String updatedTime = formatUpdatedDateTime(new Date());
                UserImportLocationRank locationRank = new UserImportLocationRank(
                        updatedTime, languageRank.getRepos(), languageRank.getStars(), locations);

                List<UserImportLocationRank> locationRanks = ranks.get(languageRank.getLanguage());
                if (locationRanks != null) {
                    locationRanks.add(locationRank);
                } else {
                    locationRanks = new ArrayList<>();
                    locationRanks.add(locationRank);
                    ranks.put(languageRank.getLanguage(), locationRanks);
                }

                //add new skills if they haven't had it yet
                if (!currentSkills.contains(languageRank.getLanguage())) {
                    currentSkills.add(languageRank.getLanguage());
                }
            }
            //update ranks for user if it hasn't already existed
            profile.put("ranks", ranks);
        }
    }

    private String formatUpdatedDateTime(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
        return sdf.format(date);
    }

    private void processLanguageRank(GithubAwardModel languageRank) {
        // ImportIO responds language with "ranking" word, remove it
        languageRank.setLanguage(languageRank.getLanguage().replace(" ranking", "").toLowerCase());

        if (StringUtils.isNotEmpty(languageRank.getCityRank())) {
            languageRank.setCityRank(languageRank.getCityRank().replaceAll(" ", ""));
        }
        if (StringUtils.isNotEmpty(languageRank.getCountryRank())) {
            languageRank.setCountryRank(languageRank.getCountryRank().replaceAll(" ", ""));
        }
        if (StringUtils.isNotEmpty(languageRank.getWorldwideRank())) {
            languageRank.setWorldwideRank(languageRank.getWorldwideRank().replaceAll(" ", ""));
        }
    }


    private int[] processRankByLocation(String locationRank) {
        if (StringUtils.isNotEmpty(locationRank)) {
            String[] tokens = locationRank.split("/");
            return Arrays.stream(tokens).mapToInt(Integer::valueOf).toArray();
        }
        return new int[]{0, 0};
    }
}
