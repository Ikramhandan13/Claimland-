package com.claimland;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

public class LandTest {
    @Test
    public void createLand() {
        UUID u = UUID.randomUUID();
        Land l = new Land("land-0001", "MyLand", u, "world", 0, 0, System.currentTimeMillis());
        assertEquals("land-0001", l.getId());
        assertEquals(u, l.getOwner());
        assertEquals("MyLand", l.getName());
    }
}
