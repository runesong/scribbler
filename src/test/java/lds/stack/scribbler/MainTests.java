package lds.stack.scribbler;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import scribbler.Scribbler;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Scribbler.class)
@WebAppConfiguration
public class MainTests {

    @Test
    public void contextLoads() {
    }
}
