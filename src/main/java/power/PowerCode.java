package power;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static constant.CommonConstant.GROUPA;
import static constant.CommonConstant.GROUPB;


public class PowerCode {


    @BeforeClass
    private void sayHello() {
        System.out.println("=======================Hello Mega=============================");
    }

    @AfterClass
    private void sayGoodbye() {
        System.out.println("=====================Bye Bye================================");
    }

    public static int getRandomNumberInRange(int min, int max) {

        if (min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }

        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }

    @Test(groups = GROUPA)
    private void testCase01()

    {
        this.createPowerCode(1, 50);
    }

    @Test(groups = GROUPB)
    private void testCase02() {
        this.createPowerCode(5, 50);
    }

    private void createPowerCode(int index, int endcode) {
        List<Integer> list = new ArrayList<Integer>();
        for (int i = 0; i < 6; i++) {
            list.add(PowerCode.getRandomNumberInRange(index, endcode));
        }
        Collections.sort(list, Collections.reverseOrder());

        for (int i = 0; i < 6; i++) {
            if (list.get(i) < 0) {
                Assert.fail(list.get(i) + "<0");
            }
            System.out.println(list.get(i));
        }
    }

}

