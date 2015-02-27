package com.techlooper.service;
import javax.validation.constraints.NotNull;
import com.techlooper.pojo.GravatarModel;
import com.techlooper.pojo.GravatarResponse;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * Created by chrisshayan on 2/21/15.
 */
@Service
public class GravatarService {

    /**
     * MD5 of email
     *
     * @param email to convert
     * @return MD5
     */
    public String getMD5Hash(@NotNull final String email) {
        try {
            final MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(email.getBytes());
            byte[] digest = md5.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Could not MD5 the email");
        }
    }

    /**
     * @param email to find the user on engravatar
     * @return instance of the user
     */
    public GravatarModel findGravatarProfile(@NotNull final String email) {
        final String restUrl = String.format("https://en.gravatar.com/%s.json", getMD5Hash(email));
        RestTemplate restTemplate = new RestTemplate();

        try {
            final List<GravatarModel> gravatarModels = restTemplate.getForObject(restUrl, GravatarResponse.class).getGravatarModels();
            if (CollectionUtils.isEmpty(gravatarModels))
                return null;
            return gravatarModels.get(0);
        } catch (RestClientException e) {
            return null;
        }
    }
}
