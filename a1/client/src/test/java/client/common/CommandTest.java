package client.common;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CommandTest {

    @Test
    void fromString_isCaseInsensitive_andThrowsOnUnknown() {
        assertEquals(Command.AddFlight, Command.fromString("addflight"));
        assertEquals(Command.AddFlight, Command.fromString("ADDFLIGHT"));
        assertThrows(IllegalArgumentException.class, () -> Command.fromString("NoPe"));
    }

    @Test
    void description_listsAllCommands_andHasHelpHint() {
        String d = Command.description();
        // Ensure a few representative commands are listed and help hint exists
        assertTrue(d.contains("AddFlight"));
        assertTrue(d.contains("QueryCustomer"));
        assertTrue(d.contains("ReserveRoom"));
        assertTrue(d.contains("Quit"));
        assertTrue(d.toLowerCase().contains("help"));
    }

    @Test
    void toString_showsUsageAndDescription() {
        String t = Command.AddRooms.toString();
        assertTrue(t.contains("AddRooms"));
        assertTrue(t.contains("Usage:"));
        assertTrue(t.contains("<Location>"));
    }
}
