package Klarna;

import static org.junit.Assert.assertTrue;
import org.junit.Test;
import Klarna.GreenCode;

public class GreenCodeTest {
  @Test
  public void test1() {

    Float[] cpuUsage = {0.3f, 0.5f, 0.9f, 0.7f, 0.1f};
    Integer[] usedHeap = {3000, 250, 100, 2000, 2000, 10246};

    assertTrue(GreenCode.verify(cpuUsage, usedHeap).equalsIgnoreCase("yellow"));
  }

  @Test
  public void test2() {

    Float[] cpuUsage = {0.3f, 0.5f, 0.8f, 0.7f, 0.1f};
    Integer[] usedHeap = {3000, 250, 22938, 2000, 2000};

    assertTrue(GreenCode.verify(cpuUsage, usedHeap).equalsIgnoreCase("red"));
  }

  @Test
  public void test3() {

    Float[] cpuUsage = {0.3f, 0.5f, 0.5f, 0.7f, 0.1f};
    Integer[] usedHeap = {3000, 4000, 7000, 2000, 2000};

    assertTrue(GreenCode.verify(cpuUsage, usedHeap).equalsIgnoreCase("yellow"));
  }

  @Test
  public void test4() {
    Float[] cpuUsage = {0.3f, 0.5f, null};
    Integer[] usedHeap = {3000, 4000, 7000, 2000, 2000};
    assertTrue(GreenCode.verify(cpuUsage, usedHeap).equalsIgnoreCase("red"));
  }

  @Test
  public void test5() {
    Float[] cpuUsage = {0.3f, 0.5f, 0.89f, 0.7f, null};
    Integer[] usedHeap = {3000, 250, 100, 100, 2000, 150};
    assertTrue(GreenCode.verify(cpuUsage, usedHeap).equalsIgnoreCase("green"));
  }

  @Test
  public void test6() {
    Float[] cpuUsage = {0.3f, 0.5f, 0.89f, 0.7f, 0.1f};
    Integer[] usedHeap = {3000, 250, 100, null, 2000, null};
    assertTrue(GreenCode.verify(cpuUsage, usedHeap).equalsIgnoreCase("red"));
  }

  @Test
  public void test7() {
    Float[] cpuUsage = {0.3f};
    Integer[] usedHeap = {3000};
    assertTrue(GreenCode.verify(cpuUsage, usedHeap).equalsIgnoreCase("green"));
  }

  @Test
  public void test8() {

    Float[] cpuUsage = {0.3f, 0.5f, 0.8f, 0.7f, 0.1f};
    Integer[] usedHeap = {3000, 250, 100, 2000, 2000, 10246};

    assertTrue(GreenCode.verify(cpuUsage, usedHeap).equalsIgnoreCase("green"));
  }

  @Test
  public void test9() {

    Float[] cpuUsage = {0.3f, 0.5f, 0.8f, 0.7f, 0.1f};
    Integer[] usedHeap = {3000, 250, 2293, 2000, 2000};

    assertTrue(GreenCode.verify(cpuUsage, usedHeap).equalsIgnoreCase("green"));
  }

  @Test
  public void test10() {

    Float[] cpuUsage = {null};
    Integer[] usedHeap = {null};

    assertTrue(GreenCode.verify(cpuUsage, usedHeap).equalsIgnoreCase("red"));
  }

  @Test
  public void test11() {

    Float[] cpuUsage = {null, null};
    Integer[] usedHeap = {null, null};

    assertTrue(GreenCode.verify(cpuUsage, usedHeap).equalsIgnoreCase("red"));
  }

  @Test
  public void test12() {

    Float[] cpuUsage = {0.3f};
    Integer[] usedHeap = {3000};

    assertTrue(GreenCode.verify(cpuUsage, usedHeap).equalsIgnoreCase("green"));
  }

  @Test
  public void test13() {
    Float[] cpuUsage = {0.3f, 0.2f};
    Integer[] usedHeap = {3000, 4000, 5000};
    assertTrue(GreenCode.verify(cpuUsage, usedHeap).equalsIgnoreCase("yellow"));
  }

  @Test
  public void test14() {
    Float[] cpuUsage = {0.3f, 0.2f};
    Integer[] usedHeap = {3000, 4000, null, null, null};
    assertTrue(GreenCode.verify(cpuUsage, usedHeap).equalsIgnoreCase("red"));
  }

  @Test
  public void test15() {
    assertTrue(GreenCode.verify(null, null).equalsIgnoreCase("red"));
  }

  @Test
  public void test16() {
    Float[] cpuUsage = {0.3f, 0.2f};
    Integer[] usedHeap = {3000, 4000, 4000, 5000, 6000};
    assertTrue(GreenCode.verify(cpuUsage, usedHeap).equalsIgnoreCase("yellow"));
  }

  @Test
  public void test17() {
    Float[] cpuUsage = {0.3f};
    Integer[] usedHeap = {3000, 4000, 4000, 25000, 6000};
    assertTrue(GreenCode.verify(cpuUsage, usedHeap).equalsIgnoreCase("yellow"));
  }

  @Test
  public void test18() {

    Float[] cpuUsage = {0.3f, 0.5f, 0.9f, 0.7f, 0.1f, 0.9f};
    Integer[] usedHeap = {3000, 250, 100, 2000, 2000, 10246};

    assertTrue(GreenCode.verify(cpuUsage, usedHeap).equalsIgnoreCase("red"));
  }
}
