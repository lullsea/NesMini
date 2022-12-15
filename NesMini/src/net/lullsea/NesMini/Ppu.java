package net.lullsea.NesMini;

import java.util.Arrays;

public class Ppu {
    Nes nes;

    // Registers
    public PPUSTATUS status;
    public PPUCTRL control;
    public PPUMASK mask;

    // NTSC Palette Table
    int[] palette = new int[] { 0x525252, 0xB40000, 0xA00000, 0xB1003D, 0x740069, 0x00005B, 0x00005F, 0x001840,
            0x002F10, 0x084A08, 0x006700, 0x124200, 0x6D2800, 0x000000, 0x000000, 0x000000, 0xC4D5E7, 0xFF4000,
            0xDC0E22, 0xFF476B, 0xD7009F, 0x680AD7, 0x0019BC, 0x0054B1, 0x006A5B, 0x008C03, 0x00AB00, 0x2C8800,
            0xA47200, 0x000000, 0x000000, 0x000000, 0xF8F8F8, 0xFFAB3C, 0xFF7981, 0xFF5BC5, 0xFF48F2, 0xDF49FF,
            0x476DFF, 0x00B4F7, 0x00E0FF, 0x00E375, 0x03F42B, 0x78B82E, 0xE5E218, 0x787878, 0x000000, 0x000000,
            0xFFFFFF, 0xFFF2BE, 0xF8B8B8, 0xF8B8D8, 0xFFB6FF, 0xFFC3FF, 0xC7D1FF, 0x9ADAFF, 0x88EDF8, 0x83FFDD,
            0xB8F8B8, 0xF5F8AC, 0xFFFFB0, 0xF8D8F8, 0x000000, 0x000000 };

    // Tables
    int[][] patternTable; // left: $0 - $fff; right: $1000 - $1fff
    Nametable[] nametable;

    public Ppu(Nes nes) {
        this.nes = nes;

        control = new PPUCTRL();
        mask = new PPUMASK();
        status = new PPUSTATUS();

        nametable = new Nametable[4];
        Arrays.fill(nametable, new Nametable());

        // 
        patternTable = new int[2][4096];
    }

    public void reset() {
    }

    /* --------------------------------- Ppu I/O -------------------------------- */

    public int read(int addr) {
        addr &= 0x3fff;
        if (addr <= 0x1fff)
            return nes.mapper.readVROM(addr);
        else if (addr <= 0x3fff)
            // TODO
            return 0;
        return 0;
    }

    public void write() {

    }

    // Int to boolean
    private boolean ib(int a) {
        return (a != 0);
    }

    // Boolean to int
    private int bi(boolean a) {
        return a ? 1 : 0;
    }

    /* ------------------------------ PPU Registers ----------------------------- */

    // Cpu $2000
    public final class PPUCTRL {
        boolean x, y, increment, spritePtr, backgroundPtr, sprSize, slave, nmi;

        PPUCTRL() {
            set(0);
        }

        public int get() {
            return (x ? 0x1 : 0) |
                    (y ? 0x2 : 0) |
                    (increment ? 0x4 : 0) |
                    (spritePtr ? 0x8 : 0) |
                    (backgroundPtr ? 0x10 : 0) |
                    (sprSize ? 0x20 : 0) |
                    (slave ? 0x40 : 0) |
                    (nmi ? 0x80 : 0);
        }

        public void set(int data) {
            x = (data & 0x1) > 0;
            y = (data & 0x2) > 0;
            increment = (data & 0x4) > 0;
            spritePtr = (data & 0x8) > 0;
            backgroundPtr = (data & 0x10) > 0;
            sprSize = (data & 0x20) > 0;
            slave = (data & 0x40) > 0;
            nmi = (data & 0x80) > 0;
        }
    }

    // Cpu $2001
    public final class PPUMASK {
        boolean grayscale, bgLeft, sprLeft, background, sprite, red, green, blue;

        PPUMASK() {
            set(0);
        }

        public int get() {
            return (grayscale ? 0x1 : 0) |
                    (bgLeft ? 0x2 : 0) |
                    (sprLeft ? 0x4 : 0) |
                    (background ? 0x8 : 0) |
                    (sprite ? 0x10 : 0) |
                    (red ? 0x20 : 0) |
                    (green ? 0x40 : 0) |
                    (blue ? 0x80 : 0);
        }

        public void set(int data) {
            grayscale = (data & 0x1) > 0;
            bgLeft = (data & 0x2) > 0;
            sprLeft = (data & 0x4) > 0;
            background = (data & 0x8) > 0;
            sprite = (data & 0x10) > 0;
            red = (data & 0x20) > 0;
            green = (data & 0x40) > 0;
            blue = (data & 0x80) > 0;
        }

    }

    // Cpu $2002
    public final class PPUSTATUS {
        int bus;
        boolean sprOverflow, spr0hit, vblank;

        PPUSTATUS() {
            bus = 0;
            sprOverflow = spr0hit = vblank = false;
        }

        public int get() {
            return bus |
                    (sprOverflow ? 0x20 : 0) |
                    (spr0hit ? 0x40 : 0) |
                    (vblank ? 0x80 : 0);
        }
    }

}

class Nametable {
    // Used for debugging
    private static int id = 0;
    String name;
    int[] tile;
    int[] attribute;

    // ===== 1024 byte area of memory ===== //
    Nametable() {
        // 30 rows of 32 tiles
        tile = new int[30 * 32];
        // 64-byte array at the end of each nametable
        attribute = new int[64];

        Arrays.fill(tile, 0);
        Arrays.fill(attribute, 0);

        name = "Nametable" + id;
        id += 1;
    }

}