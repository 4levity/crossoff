package org.pricelessfestival.crossoff.server;

import lombok.extern.log4j.Log4j2;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;

/**
 * Created by ivan on 5/6/18.
 */
@Log4j2
public abstract class CrossoffTests {

    @Rule
    public TestName name = new TestName();

    @Before
    public void setup() {
        log.info("***** STARTING TEST {} *****", name.getMethodName());
    }
}
