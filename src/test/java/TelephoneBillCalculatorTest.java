import com.phonecompany.billing.TelephoneBillCalculator;
import com.phonecompany.billing.TelephoneBillCalculatorImpl;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TelephoneBillCalculatorTest {
    TelephoneBillCalculator calculator = new TelephoneBillCalculatorImpl();

    @Test
    void twoMostFrequentlyCalledNumbers () {
        String phoneLog = """
                420774577453,13-01-2020 18:10:15,13-01-2020 18:12:57
                420776562353,18-01-2020 08:59:20,18-01-2020 09:10:00
                """;
        BigDecimal result = calculator.calculate(phoneLog);
        assertEquals(new BigDecimal("1.5"), result);
    }

    @Test
    void oneMostFrequentlyCalledNumber () {
        String phoneLog = """
                420774577453,13-01-2020 18:10:15,13-01-2020 18:12:57
                420774577453,18-01-2020 08:59:20,18-01-2020 09:10:00
                """;
        BigDecimal result = calculator.calculate(phoneLog);
        assertEquals(new BigDecimal("0"), result);
    }

    @Test
    void callDuringPeakHoursCostsOnePerMinute() {
        String phoneLog = """
        420700000001,13-01-2020 08:00:00,13-01-2020 08:05:00
        420774577453,13-01-2020 18:10:15,13-01-2020 18:12:57
        420774577453,18-01-2020 08:59:20,18-01-2020 09:10:00""";

        BigDecimal result = calculator.calculate(phoneLog);
        assertEquals(new BigDecimal("5.0"), result);
    }

    @Test
    void callDuringOffPeakHoursCostsHalfPerMinute() {
        String phoneLog = """
        420700000002,13-01-2020 06:00:00,13-01-2020 06:05:00
        420774577453,13-01-2020 18:10:15,13-01-2020 18:12:57
        420774577453,18-01-2020 08:59:20,18-01-2020 09:10:00""";

        BigDecimal result = calculator.calculate(phoneLog);
        assertEquals(new BigDecimal("2.5"), result);
    }

    @Test
    void callCrossingPeakAndOffPeakChargesEachMinuteCorrectly() {
        String phoneLog = """
        420700000003,13-01-2020 07:46:00,13-01-2020 08:02:00
        420774577453,13-01-2020 18:10:15,13-01-2020 18:12:57
        420774577453,18-01-2020 08:59:20,18-01-2020 09:10:00
        """;

        BigDecimal result = calculator.calculate(phoneLog);
        assertEquals(new BigDecimal("4.7"), result);
    }

    @Test
    void callLongerThanFiveMinutesAppliesReducedRateAfterFive() {
        String phoneLog = """
        420700000004,13-01-2020 10:00:00,13-01-2020 10:08:00
        420774577453,13-01-2020 18:10:15,13-01-2020 18:12:57
        420774577453,18-01-2020 08:59:20,18-01-2020 09:10:00""";

        BigDecimal result = calculator.calculate(phoneLog);
        assertEquals(new BigDecimal("5.6"), result);
    }

    @Test
    void callWithSecondsIsRoundedUpToNextMinute() {
        String phoneLog = """
        420700000009,13-01-2020 08:00:00,13-01-2020 08:00:10
        420774577453,13-01-2020 18:10:15,13-01-2020 18:12:57
        420774577453,18-01-2020 08:59:20,18-01-2020 09:10:00""";

        BigDecimal result = calculator.calculate(phoneLog);
        assertEquals(new BigDecimal("1.0"), result);
    }
}
