package com.techlooper.service;

import com.benfante.jslideshare.SlideShareAPI;
import com.benfante.jslideshare.SlideShareAPIFactory;
import com.benfante.jslideshare.messages.Tag;
import com.benfante.jslideshare.messages.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;

import javax.validation.constraints.NotNull;

/**
 * Created by chrisshayan on 3/1/15.
 */
@PropertySource("classpath:slideshare.properties")
public class SlideShareService {

    @Value("${api.key}")
    private String key;

    @Value("${shared.secret}")
    private String secret;

    /**
     * Search on SlideShare slides
     * @param tag the tag to be used for searching on SlideShare
     * @param offset The offset from which retrieving the slideshows. Starting from 0. A negative value means no offset.
     * @param limit How many slideshows to retrieve. A negative value means no limit.
     * @return See {@linkplain com.benfante.jslideshare.messages.Tag}
     */
    public Tag searchByTag(@NotNull final String tag, final int offset, final int limit) {
        final SlideShareAPI slideShareAPI = getSlideShareAPI();
        return slideShareAPI.getSlideshowByTag(tag, offset, limit);
    }

    /**
     * Retrieve slideshows for a given user.
     *
     * @param username The username of the user.
     * @return The user data (name, slideshows, etc).
     * @throws com.benfante.jslideshare.SlideShareException In case of a SlideShareServiceError
     * @throws com.benfante.jslideshare.SlideShareErrorException In case of an error using the service (IO error, timeouts, http status other than OK, etc.)
     */
    public User searchByUsername(@NotNull final String username) {
        return getSlideShareAPI().getSlideshowByUser(username);
    }

    private SlideShareAPI getSlideShareAPI() {
        return SlideShareAPIFactory.getSlideShareAPI(key, secret);
    }

}
