package com.techlooper.service;

import com.benfante.jslideshare.SlideShareAPI;
import com.benfante.jslideshare.SlideShareAPIFactory;
import com.benfante.jslideshare.messages.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;

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
    public Tag searchByTag(final String tag, final int offset, final int limit) {
        final SlideShareAPI slideShareAPI = SlideShareAPIFactory.getSlideShareAPI(key, secret);
        return slideShareAPI.getSlideshowByTag(tag, offset, limit);
    }

}
