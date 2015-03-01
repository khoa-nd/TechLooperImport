package com.techlooper.service;


import com.benfante.jslideshare.messages.Slideshow;
import com.benfante.jslideshare.messages.Tag;
import com.techlooper.configuration.SlideShareConfiguration;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SlideShareServiceTest {

    @Test
    public void testSearchByTag() throws Exception {
        final ApplicationContext applicationContext = new AnnotationConfigApplicationContext(SlideShareConfiguration.class);
        final SlideShareService service = (SlideShareService) applicationContext.getBean("slideShareService");
        final Tag java = service.searchByTag("java", 0, 10);

        assertNotNull(java);
        assertTrue("java".equals(java.getName()));

        final List<Slideshow> slideshows = java.getSlideshows();
        assertNotNull(slideshows);
        slideshows.stream().forEach(slideshow -> {
            assertNotNull(slideshow.getTitle());
            System.out.println("slideshow.getTitle() = " + slideshow.getTitle());
        });
    }
}
