package com.techlooper.service;


import com.benfante.jslideshare.messages.Slideshow;
import com.benfante.jslideshare.messages.Tag;
import com.benfante.jslideshare.messages.User;
import com.techlooper.configuration.SlideShareConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SlideShareServiceTest {

    ApplicationContext applicationContext;
    SlideShareService service;

    @Before
    public void setApplicationContext() {
        this.applicationContext = new AnnotationConfigApplicationContext(SlideShareConfiguration.class);
        this.service =  (SlideShareService) applicationContext.getBean("slideShareService");
    }

    @Test
    public void testSearchByTag() throws Exception {
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

    @Test
    public void testSearchByUsername() throws Exception {
        final User chrisshayan = service.searchByUsername("chrisshayan");
        assertNotNull(chrisshayan);
        assertEquals("chrisshayan", chrisshayan.getName());

        final List<Slideshow> slideshows = chrisshayan.getSlideshows();
        assertNotNull(slideshows);
        slideshows.stream().forEach(slideshow -> {
            assertNotNull(slideshow.getTitle());
            System.out.println("slideshow.getTitle() = " + slideshow.getTitle());
        });
    }

    @Test(expected = Exception.class)
    public void testSearchByUsernameException() throws Exception {
        service.searchByUsername("chrisshayanssssss");
    }
}
