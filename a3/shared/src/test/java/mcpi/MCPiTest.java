package mcpi;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class MCPiTest {

    @Test
    void testPi() {
        long n = 1_000_000;
        MCPi mcpi = new MCPi(n);
        assertDoesNotThrow(() -> mcpi.compute());
        assertEquals(3.14159, mcpi.getPi(), 0.05);
    }
}
