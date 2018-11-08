package org.xmeng.xmeng;

import org.junit.Assert;
import org.junit.Test;


public class DataTest {

    @Test
    public void testInValidData() {
        Data data = new Data();
        data.longitude = 0;
        data.latitude = 0;

        Assert.assertFalse(data.valid());

        data.longitude = 53;

        Assert.assertFalse(data.valid());

        data.longitude = 51.8;
        Assert.assertFalse(data.valid());
    }

    @Test
    public void testValidData() {
        Data data = new Data();
        data.longitude = 4;
        data.latitude = 51.8;

        Assert.assertTrue(data.valid());

        data.longitude = 3.9;
        data.latitude = 51.9;

        Assert.assertTrue(data.valid());

        data.longitude = 4.271833419799805;
        data.latitude =  51.87966537475586;
        Assert.assertTrue(data.valid());
    }
}
