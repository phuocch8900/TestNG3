package power;


import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class PowerCode {
    @BeforeClass
    private void sayHello() {
        System.out.println("Chào mừng đến với chương trình xổ số PowerCode");
    }

    @AfterClass
    private void sayGoodbye() {
        System.out.println("Cám ơn đến vơi chương trình xổ số và xin hẹn gặp lại");
    }

    public static int getRandomNumberInRange(int min, int max) {

        if (min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }

        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }

    @Test
    private void testCase01()

    {
        this.createPowerCode(1, 50);
    }

    @Test
    private void testCase02() {
        this.createPowerCode(-10, 40);
    }

    private void createPowerCode(int index, int endcode) {
        List<Integer> list = new ArrayList<Integer>();
        for (int i = 0; i < 6; i++) {
            list.add(PowerCode.getRandomNumberInRange(index, endcode));
        }
        Collections.sort(list, Collections.reverseOrder());

        for (int i = 0; i < 6; i++) {
            if (list.get(i) < 0) {
                Assert.fail("Ket qua so xo so khong the ra am: " + list.get(i));
            }
            System.out.println(list.get(i));
        }
    }

}

