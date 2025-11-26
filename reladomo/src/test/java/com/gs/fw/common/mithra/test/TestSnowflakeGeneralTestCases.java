
/*
 Copyright 2016 Goldman Sachs.
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.
 */

package com.gs.fw.common.mithra.test;

import java.sql.Timestamp;
import java.text.ParseException;

import org.eclipse.collections.impl.set.mutable.UnifiedSet;
import org.eclipse.collections.impl.set.mutable.primitive.IntHashSet;

import com.gs.fw.common.mithra.MithraManagerProvider;
import com.gs.fw.common.mithra.finder.Operation;
import com.gs.fw.common.mithra.test.aggregate.TestStandardDeviation;
import com.gs.fw.common.mithra.test.aggregate.TestVariance;
import com.gs.fw.common.mithra.test.domain.AllTypesFinder;
import com.gs.fw.common.mithra.test.domain.AllTypesList;
import com.gs.fw.common.mithra.test.domain.Order;
import com.gs.fw.common.mithra.test.domain.OrderFinder;
import com.gs.fw.common.mithra.test.domain.OrderList;
import com.gs.fw.common.mithra.util.DoWhileProcedure;
import com.gs.fw.common.mithra.util.TempTableNamer;

public class TestSnowflakeGeneralTestCases extends MithraSnowflakeTestAbstract
{
    // Snowflake's STDDEV_POP over integer-like columns (after the CAST to DOUBLE in
    // the adapter) differs from the reference value by ~2e-7. Use a slightly looser
    // bound for those Snowflake-specific stddev_pop tests.
    private static final double DELTA = 0.000001;

    public void tearDown() throws Exception
    {
        MithraManagerProvider.getMithraManager().setTransactionTimeout(60);
        super.tearDown();
    }

    public void testRollback()
    {
        new CommonVendorTestCases().testRollback();
    }

    public void testTimestampGranularity() throws Exception
    {
        new CommonVendorTestCases().testTimestampGranularity();
    }

    public void testOptimisticLocking() throws Exception
    {
        new CommonVendorTestCases().testOptimisticLocking();
    }

    public void testTimeTransactionalBasicTime()
    {
        new TestTimeTransactional().testBasicTime();
    }

    public void testTimeTransactionalToString()
    {
        new TestTimeTransactional().testToString();
    }

    public void testTimeTransactionalInsert()
    {
        new TestTimeTransactional().testInsert();
    }

    public void testTimeTransactionalDelete()
    {
        new TestTimeTransactional().testDelete();
    }

    public void testTimeTransactionalUpdate()
    {
        new TestTimeTransactional().testUpdate();
    }

    public void testTimeTransactionalNullTime()
    {
        new TestTimeTransactional().testNullTime();
    }

    public void testTimeNonTransactionalBasicTime()
    {
        new TestTimeNonTransactional().testBasicTime();
    }

    public void testTimeNonTransactionalToString()
    {
        new TestTimeNonTransactional().testToString();
    }

    public void testTimeNonTransactionalNullTime()
    {
        new TestTimeNonTransactional().testNullTime();
    }

    public void testTimeDatedTransactionalBasicTime()
    {
        new TestTimeDatedTransactional().testBasicTime();
    }

    public void testTimeDatedTransactionalUpdateTime()
    {
        new TestTimeDatedTransactional().testUpdate();
    }

    public void testTimeDatedTransactionalToString()
    {
        new TestTimeDatedTransactional().testToString();
    }

    public void testTimeDatedTransactionalInsert()
    {
        new TestTimeDatedTransactional().testInsert();
    }

    public void testTimeDatedTransactionalTerminate()
    {
        new TestTimeDatedTransactional().testTerminate();
    }

    public void testTimeDatedTransactionalNullTime()
    {
        new TestTimeDatedTransactional().testNullTime();
    }

    public void testTimeDatedNonTransactionalBasicTime()
    {
        new TestTimeDatedNonTransactional().testBasicTime();
    }

    public void testTimeDatedNonTransactionalToString()
    {
        new TestTimeDatedNonTransactional().testToString();
    }

    public void testTimeDatedNonTransactionalNullTime()
    {
        new TestTimeDatedNonTransactional().testNullTime();
    }

    public void testTimeBitemporalTransactionalBasicTime() 
    {
        new TestTimeBitemporalTransactional().testBasicTime();
    }

    public void testTimeBitemporalTransactionalToString() 
    {
        new TestTimeBitemporalTransactional().testToString();
    }

    public void testTimeBitemporalTransactionalInsert() 
    {
        new TestTimeBitemporalTransactional().testInsert();
    }

    public void testTimeBitemporalTransactionalTerminate() 
    {
        new TestTimeBitemporalTransactional().testTerminate();
    }

    public void testTimeBitemporalTransactionalUpdate() 
    {
        new TestTimeBitemporalTransactional().testUpdate();
    }

    public void testTimeBitemporalTransactionalUpdateUntil() 
    {
        new TestTimeBitemporalTransactional().testUpdateUntil();
    }

    public void testTimeBitemporalTransactionalInsertUntil() 
    {
        new TestTimeBitemporalTransactional().testInsertUntil();
    }

    // Can't terminateUntil on an in-memory object
    // public void testTimeBitemporalTransactionalTerminateUntil() 
    // {
    //     new TestTimeBitemporalTransactional().testTerminateUntil();
    // }

    public void testTimeBitemporalTransactionalNullTime() 
    {
        new TestTimeBitemporalTransactional().testNullTime();
    }

    public void testStandardDeviationDouble() throws ParseException
    {
        new TestStandardDeviation().testStdDevDouble();
    }

    public void testStandardDeviationPopDouble() throws ParseException
    {
        new TestStandardDeviation().testStdDevPopDouble();
    }

    public void testStandardDeviationInt() throws ParseException
    {
        new TestStandardDeviation().testStdDevInt();
    }

    public void testStandardDeviationPopInt() throws ParseException
    {
        new TestStandardDeviation(DELTA).testStdDevPopInt();
    }

    public void testStandardDeviationLong() throws ParseException
    {
        new TestStandardDeviation().testStdDevLong();
    }

    public void testStandardDeviationPopLong() throws ParseException
    {
        new TestStandardDeviation(DELTA).testStdDevPopLong();
    }

    public void testStandardDeviationShort() throws ParseException
    {
        new TestStandardDeviation().testStdDevShort();
    }

    public void testStandardDeviationPopShort() throws ParseException
    {
        new TestStandardDeviation(DELTA).testStdDevPopShort();
    }

    public void testStandardDeviationByte() throws ParseException
    {
        new TestStandardDeviation().testStdDevByte();
    }

    public void testStandardDeviationPopByte() throws ParseException
    {
        new TestStandardDeviation(DELTA).testStdDevPopByte();
    }

    public void testStandardDeviationFloat() throws ParseException
    {
        new TestStandardDeviation().testStdDevFloat();
    }

    public void testStandardDeviationPopFloat() throws ParseException
    {
        new TestStandardDeviation().testStdDevPopFloat();
    }

    public void testStandardDeviationBigDecimal() throws ParseException
    {
        new TestStandardDeviation().testStdDevBigDecimal();
    }

    public void testStandardDeviationPopBigDecimal() throws ParseException
    {
        new TestStandardDeviation().testStdDevPopBigDecimal();
    }

    public void testVarianceDouble() throws ParseException
    {
        new TestVariance().testVarianceDouble();
    }

    public void testVariancePopDouble() throws ParseException
    {
        new TestVariance().testVariancePopDouble();
    }

    public void testVarianceInt() throws ParseException
    {
        new TestVariance().testVarianceInt();
    }

    public void testVariancePopInt() throws ParseException
    {
        new TestVariance().testVariancePopInt();
    }

    public void testVarianceLong() throws ParseException
    {
        new TestVariance().testVarianceLong();
    }

    public void testVariancePopLong() throws ParseException
    {
        new TestVariance().testVariancePopLong();
    }

    public void testVarianceShort() throws ParseException
    {
        new TestVariance().testVarianceShort();
    }

    public void testVariancePopShort() throws ParseException
    {
        new TestVariance().testVariancePopShort();
    }

    public void testVarianceByte() throws ParseException
    {
        new TestVariance().testVarianceByte();
    }

    public void testVariancePopByte() throws ParseException
    {
        new TestVariance().testVariancePopByte();
    }

    public void testVarianceFloat() throws ParseException
    {
        new TestVariance().testVarianceFloat();
    }

    public void testVariancePopFloat() throws ParseException
    {
        new TestVariance().testVariancePopFloat();
    }

    public void testVarianceBigDecimal() throws ParseException
    {
        new TestVariance().testVarianceBigDecimal();
    }

    public void testVariancePopBigDecimal() throws ParseException
    {
        new TestVariance().testVariancePopBigDecimal();
    }

    public void testTimestampMonthDayOfMonthTuplesTiny()
    {
        new TestYearMonthTuple().testTimestampTupleMonthDayOfMonthTinySet();
    }

    public void testTimestampYearDayOfMonthTuplesTiny()
    {
        new TestYearMonthTuple().testTimestampTupleYearDayOfMonthTinySet();
    }

    public void testTimestampYearMonthTuplesTiny()
    {
        new TestYearMonthTuple().testTimestampTupleYearMonthTinySet();
    }

    public void testDateMonthDayOfMonthTuplesTiny()
    {
        new TestYearMonthTuple().testTupleMonthDayOfMonthTinySet();
    }

    public void testDateYearDayOfMonthTuplesTiny()
    {
        new TestYearMonthTuple().testTupleYearDayOfMonthTinySet();
    }

    public void testDateYearMonthTuplesTiny()
    {
        new TestYearMonthTuple().testTupleYearMonthTinySet();
    }

    public void testDateYearMonthTuplesValueOf()
    {
        new TestYearMonthTuple().testValueOf();
    }

    public void testDateYearMonthTuples()
    {
        new TestYearMonthTuple().testTupleYearMonthSet();
    }

    public void testTimestampTuplesValueOf()
    {
        new TestYearMonthTuple().testTimestampValueOf();
    }

    public void testTimestampYearMonthTuples()
    {
        new TestYearMonthTuple().testTimestampTupleYearMonthSet();
    }

    public void testStringLikeEscapes()
    {
        new TestStringLike().testStringLikeEscapes();
    }

    public void testTimeTuples()
    {
        new TestTimeTuple().testTupleSet();
    }

    public void testBasicRetrievalMaxObjectsToRetrieve()
    {
        new TestBasicRetrieval().testMaxObjectsToRetrieve();
    }

    public void testRetrieveLimitedRows()
    {
        int id = 9876;
        Operation op = AllTypesFinder.id().greaterThanEquals(id);
        AllTypesList list = new AllTypesList(op);
        list.setMaxObjectsToRetrieve(5);
        list.setOrderBy(AllTypesFinder.id().ascendingOrderBy());
        assertEquals(5, list.size());
    }

    public void testTempTableNamer()
    {
        String first = TempTableNamer.getNextTempTableName();
        for (int i = 0; i < 1000000; i++)
        {
            assertFalse(first.equals(TempTableNamer.getNextTempTableName()));
        }
        UnifiedSet<String> set = new UnifiedSet<>(10000);
        for (int i = 0; i < 10000; i++)
        {
            String s = TempTableNamer.getNextTempTableName();
            assertFalse(set.contains(s));
            set.add(s);
        }
    }

    public void testSybaseTopQuery()
    {
        OrderList list = new OrderList(OrderFinder.orderId().lessThan(5));
        list.setMaxObjectsToRetrieve(1);
        assertEquals(1, list.size());
    }

    public void testSybaseTopQueryWithCursor()
    {
        OrderList list = new OrderList(OrderFinder.orderId().lessThan(5));
        list.setMaxObjectsToRetrieve(1);
        final int[] count = new int[1];
        list.forEachWithCursor(new DoWhileProcedure()
        {
            public boolean execute(Object object)
            {
                Order o = (Order) object;
                count[0]++;
                return true;
            }
        });
        assertEquals(1, count[0]);
    }

    public void testMod()
    {
        TestCalculated testCalculated = new TestCalculated();
        testCalculated.testMod();
    }

    public void testReadInNewYork() throws ParseException
    {
        Timestamp ts = new Timestamp(timestampFormat.parse("2008-04-01 00:00:00.0").getTime());
        Order newOrder = new Order();
        newOrder.setOrderId(999);
        newOrder.setState("Some state");
        newOrder.setTrackingId("Tracking Id");
        newOrder.setUserId(1);
        newOrder.setOrderDate(ts);

        newOrder.insert();
        OrderFinder.clearQueryCache();
        Order order = OrderFinder.findOne(OrderFinder.orderId().eq(999));
        assertEquals(ts, order.getOrderDate());
    }

    public void testDeleteAllInBatches()
    {
        OrderList list = createOrderList(5000, 1000);
        list.bulkInsertAll();
        OrderList firstList = new OrderList(OrderFinder.userId().eq(999));
        firstList.deleteAllInBatches(500);
        validateMithraResult(OrderFinder.userId().eq(999), "SELECT * FROM ORDERS WHERE USER_ID = 999", 0);
    }

    public void testDeleteAllInBatchesWithIn()
    {
        OrderList list = createOrderList(5000, 1000);
        list.bulkInsertAll();

        IntHashSet ids = new IntHashSet(5000);
        for (int i = 1000; i < (6000); i++)
        {
            ids.add(i);
        }

        OrderList firstList = new OrderList(OrderFinder.orderId().in(ids));
        firstList.deleteAllInBatches(500);
        validateMithraResult(OrderFinder.userId().eq(999), "SELECT * FROM ORDERS WHERE USER_ID = 999", 0);
    }
}