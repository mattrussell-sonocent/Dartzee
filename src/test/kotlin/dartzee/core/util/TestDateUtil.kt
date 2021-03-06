package dartzee.core.util

import dartzee.core.util.DateStatics.Companion.END_OF_TIME
import dartzee.helper.AbstractTest
import io.kotlintest.matchers.boolean.shouldBeFalse
import io.kotlintest.matchers.boolean.shouldBeTrue
import io.kotlintest.matchers.string.shouldBeEmpty
import io.kotlintest.shouldBe
import org.junit.Test
import java.sql.Timestamp
import java.time.ZonedDateTime

class TestDateUtil: AbstractTest()
{
    @Test
    fun testGetSqlDateNow()
    {
        val now = ZonedDateTime.now()

        val dtNow = getSqlDateNow()
        val convertedDtNow = dtNow.toLocalDateTime()

        now.year shouldBe  convertedDtNow.year
        now.month shouldBe convertedDtNow.month
        now.dayOfMonth shouldBe convertedDtNow.dayOfMonth
        now.hour shouldBe convertedDtNow.hour
    }

    @Test
    fun testEndOfTimeStr()
    {
        val str = getEndOfTimeSqlString()

        str.shouldBe("'9999-12-31 00:00:00'")
    }

    @Test
    fun testFormatHHMMSS()
    {
        val formattedOne = formatHHMMSS(18540000.4) //5h9m0s
        val formattedRounded = formatHHMMSS(18540999.9) //5h9m0s (it floors it)

        val formattedBigNumbers = formatHHMMSS(55751000.0) //15h29m11s

        formattedOne shouldBe "05:09:00"
        formattedRounded shouldBe "05:09:00"
        formattedBigNumbers shouldBe "15:29:11"
    }

    @Test
    fun testIsEndOfTime()
    {
        isEndOfTime(null).shouldBeFalse()
        isEndOfTime(getSqlDateNow()).shouldBeFalse()
        isEndOfTime(END_OF_TIME).shouldBeTrue()
    }

    @Test
    fun testIsOnOrAfter()
    {
        val nowMillis = System.currentTimeMillis()
        val dtNow = Timestamp(nowMillis)
        val slightlyLater = Timestamp(nowMillis + 1)
        val slightlyBefore = Timestamp(nowMillis - 1)

        isOnOrAfter(null, dtNow).shouldBeFalse()
        isOnOrAfter(dtNow, null).shouldBeFalse()
        isOnOrAfter(null, null).shouldBeFalse()

        isOnOrAfter(dtNow, dtNow).shouldBeTrue()
        isOnOrAfter(getSqlDateNow(), dtNow).shouldBeTrue()
        isOnOrAfter(END_OF_TIME, dtNow).shouldBeTrue()
        isOnOrAfter(dtNow, slightlyBefore).shouldBeTrue()

        isOnOrAfter(dtNow, slightlyLater).shouldBeFalse()
    }

    @Test
    fun testTimestampFormats()
    {
        val millis = 1545733545000 //25/12/2018 10:25:45

        val timestamp = Timestamp(millis)

        timestamp.formatAsDate() shouldBe "25/12/2018"
        timestamp.formatTimestamp() shouldBe "25-12-2018 10:25"
    }

    @Test
    fun testEndOfTimeFormats()
    {
        END_OF_TIME.formatAsDate().shouldBeEmpty()
        END_OF_TIME.formatTimestamp().shouldBeEmpty()
    }
}