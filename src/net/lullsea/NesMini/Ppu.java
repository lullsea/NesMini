package net.lullsea.NesMini;

import java.util.Arrays;

import net.lullsea.NesMini.RomLoader.Mirror;

public class Ppu {
    Nes nes;

    // Registers
    public PPUSTATUS status;
    public PPUCTRL control;
    public PPUMASK mask;

    // NTSC Palette Table
    int[] palette = { 0x525252, 0xB40000, 0xA00000, 0xB1003D, 0x740069, 0x00005B, 0x00005F, 0x001840, 0x002F10, 0x084A08, 0x006700, 0x124200, 0x6D2800, 0x000000, 0x000000, 0x000000, 0xC4D5E7, 0xFF4000, 0xDC0E22, 0xFF476B, 0xD7009F, 0x680AD7, 0x0019BC, 0x0054B1, 0x006A5B, 0x008C03, 0x00AB00, 0x2C8800, 0xA47200, 0x000000, 0x000000, 0x000000, 0xF8F8F8, 0xFFAB3C, 0xFF7981, 0xFF5BC5, 0xFF48F2, 0xDF49FF, 0x476DFF, 0x00B4F7, 0x00E0FF, 0x00E375, 0x03F42B, 0x78B82E, 0xE5E218, 0x787878, 0x000000, 0x000000, 0xFFFFFF, 0xFFF2BE, 0xF8B8B8, 0xF8B8D8, 0xFFB6FF, 0xFFC3FF, 0xC7D1FF, 0x9ADAFF, 0x88EDF8, 0x83FFDD, 0xB8F8B8, 0xF5F8AC, 0xFFFFB0, 0xF8D8F8, 0x000000, 0x000000 };
    int[] paletteTable;

    // left: $0 - $fff; right: $1000 - $1fff
    int[][] patternTable;
    Nametable[] nametable;
    int[] arr;
    Mirror mirror;

    // Rendering Variables
    int cycles, scanline;

    // Background
    public AddressRegister vramAddr, tramAddr;
    public boolean firstwrite;
    public int fineX; // Scrolling
    int patternLow, patternHigh, paletteLow, paletteHigh;

    // Sprite

    int[] frame; // Rendered image

    public Ppu(Nes nes) {
        this.nes = nes;

        control = new PPUCTRL();
        mask = new PPUMASK();
        status = new PPUSTATUS();

        paletteTable = new int[32];
        nametable = new Nametable[4];
        // Arrays.fill(nametable, new Nametable());
        for (int i = 0; i < 4; i++)
            nametable[i] = new Nametable();

        mirror = nes.rom.mirror;

        // Nametable mirroring
        switch (mirror) {
            case HORIZONTAL -> arr = new int[] { 0, 0, 1, 1 };
            case VERTICAL -> arr = new int[] { 0, 1, 0, 1 };
            case SINGLE -> arr = new int[] { 0, 0, 0, 0 };
            case FOUR_SCREEN -> arr = new int[] { 1, 2, 3, 4 };
            case UNLOADED -> {
                break;
            }
        }

        patternTable = new int[2][4096];
        _current = new int[2][128 * 128];

        frame = new int[256 * 240];
        cycles = 1;
        scanline = 0;

        vramAddr = tramAddr = new AddressRegister();
    }

    public void process() {
        frame[(cycles - 1) + (scanline * 256)] = palette[(int) (Math.random() * 0x3F)];

        cycles++;
        if (cycles > 256) {
            cycles = 1;
            scanline++;
            if (scanline > 239)
                scanline = 0;
        }

        /* ------- Background evaluation ------- */
        // TODO
        if ((cycles >= 0 && cycles <= 256) || (cycles >= 321 && cycles <= 336)) {
            // Each memory access takes 2 PPU cycles to complete
            switch (cycles % 8) {
                case 0:
                    // The data for the first two tiles is loaded into the shift registers
                    break;
                case 2:
                    // Fetch the corresponding attribute table entry from $23C0-$2FFF and increment
                    // the current VRAM address within the same row.
                    // Loads the next attribute to register
                    read(nametable[vramAddr.select].getAttr(
                            ((vramAddr.coarseY << 1) & 0b111000) |
                                    (vramAddr.coarseX & 0b000111)));

                    break;
            }
        }
    }

    /* ---------------------------------- debug --------------------------------- */

    public void reset() {
        _updateSpritesheet(0, 4);
        _updateSpritesheet(1, 4);
    }

    int[][] _current;

    private void _updateSpritesheet(int index, int pal) {
        // The pattern table is divided into two 256-tile sections 16x16
        // Each tile in the pattern table is 16 bytes which are separated to left and
        // right planes
        // Each plane is 8x8 bits
        // The first plane controls bit 0 of the color.
        // the second plane controls bit 1. Any pixel whose color is 0 is
        // background/transparent
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                // X here represents a tile and Y represents the 256-tile section
                int offset = y * 256 + x * 16;
                for (int i = 0; i < 8; i++) {
                    // Get the first plane
                    int low = read((index * 0x1000) + i + offset);
                    // Get the second plane
                    int high = read((index * 0x1000) + i + offset + 8);
                    for (int j = 0; j < 8; j++) {
                        int point = (low & 0x1) + (high & 0x1);
                        low >>= 1;
                        high >>= 1;
                        _current[index][(x * 8 + (7 - j)) + (((y * 8) + i) * 128)] = palette[(pal << 2) + point];
                    }
                }
            }
        }
    }

    /* --------------------------------- Ppu I/O -------------------------------- */

    public int read(int addr) {
        int tmp = 0;
        addr &= 0x3fff;

        // $0 - $1fff, Video ROM / Pattern Table
        if (addr <= 0x1fff)
            tmp = nes.mapper.readVROM(addr);
        else if (addr <= 0x3eff) {
            // Nametables: $2000 - $2fff
            // Mirrored: $3000 - $3eff
            addr &= 0xfff;

            if (addr <= 0x3ff)
                tmp = nametable[arr[0]].get(addr);
            else if (addr <= 0x7ff)
                tmp = nametable[arr[1]].get(addr);
            else if (addr <= 0xbff)
                tmp = nametable[arr[2]].get(addr);
            else if (addr <= 0xfff)
                tmp = nametable[arr[3]].get(addr);

        } else if (addr <= 0x3fff) {
            // Palette indexes
            addr &= 0x1f;
            // $3F10,$3F14,$3F18,$3F1C are mirrors of $3F00,$3F04,$3F08,$3F0C
            switch (addr) {
                case 0x10 -> addr = 0x0;
                case 0x14 -> addr = 0x4;
                case 0x18 -> addr = 0x8;
                case 0x1c -> addr = 0xc;
            }

            tmp = paletteTable[addr] & (mask.grayscale ? 0x30 : 0x3f);
        }
        // System.out.println("addr: " + "$" + Integer.toHexString(addr) + " val: " +
        // "$" + Integer.toHexString(tmp));
        return tmp & 0xff;
    }

    public void write(int addr, int data) {
        addr &= 0x3fff;
        data &= 0xff;
        if (addr <= 0x1fff)
            // Cannot write to vrom
            return;
        else if (addr <= 0x3eff) {
            addr &= 0xfff;

            if (addr <= 0x3ff)
                nametable[arr[0]].set(addr, data);
            else if (addr <= 0x7ff)
                nametable[arr[1]].set(addr, data);
            else if (addr <= 0xbff)
                nametable[arr[2]].set(addr, data);
            else if (addr <= 0xfff)
                nametable[arr[3]].set(addr, data);

        } else if (addr <= 0x3fff) {
            addr &= 0x1f;
            switch (addr) {
                case 0x10 -> addr = 0x0;
                case 0x14 -> addr = 0x4;
                case 0x18 -> addr = 0x8;
                case 0x1c -> addr = 0xc;
            }
            paletteTable[addr] = data;
        }
    }
    /* ------------------------------ PPU Registers ----------------------------- */

    // Cpu $2000
    public final class PPUCTRL {
        int select;
        boolean increment, spritePtr, backgroundPtr, sprSize, slave, nmi;

        PPUCTRL() {
            set(0);
        }

        public int get() {
            return select |
                    (increment ? 0x4 : 0) |
                    (spritePtr ? 0x8 : 0) |
                    (backgroundPtr ? 0x10 : 0) |
                    (sprSize ? 0x20 : 0) |
                    (slave ? 0x40 : 0) |
                    (nmi ? 0x80 : 0);
        }

        public void set(int data) {
            select = data & 0x3;
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
        boolean sprOverflow, spr0hit, vblank;

        PPUSTATUS() {
            sprOverflow = spr0hit = vblank = false;
        }

        public int get() {
            // The bottom 5 bits are unused
            // vblank is set to false everytime the register is read
            int tmp = (sprOverflow ? 0x20 : 0) |
                    (spr0hit ? 0x40 : 0) |
                    (!vblank ? 0x80 : 0);

            vblank = false;
            return tmp;
        }
    }

    public final class AddressRegister {
        public int coarseX, coarseY, select, fineY;

        AddressRegister() {
            set(0);
        }

        public void set(int data) {
            data &= 0x3fff;
            coarseX = (data & 0x1f);
            coarseY = (data & 0x3e0) >> 5;
            select = (data & 0xc00) >> 10;
            fineY = (data & 0x7000) >> 12;
        }

        public int get() {
            return (coarseX |
                    (coarseY << 5) |
                    (select << 10) |
                    (fineY << 12));
        }

        public void increment(boolean isHorizontal) {
            if (isHorizontal) {
                if (vramAddr.coarseX == 31) {
                    vramAddr.coarseX = 0;
                    vramAddr.select ^= 1; // Switch horizontal nametable
                    return;
                } else
                    vramAddr.coarseX += 1;
            } else {
                if (vramAddr.fineY < 7)
                    vramAddr.fineY += 1;
                else {
                    vramAddr.fineY = 0;
                    if (vramAddr.coarseY == 29) {
                        vramAddr.coarseY = 0;
                        vramAddr.select ^= 0x2;
                    } else if (vramAddr.coarseY == 31)
                        vramAddr.coarseY = 0;
                    else
                        vramAddr.coarseY += 1;
                }
                // vramAddr.set((vramAddr.get() & ~0x3E0) | (vramAddr.coarseY << 5));
            }
        }
    }

    private final class Nametable {
        // Used for debugging
        private static int id = 0;
        String name;
        private int[] tile;
        private int[] attribute;

        // ===== 1024 byte area of memory ===== //
        Nametable() {
            // 30 rows of 32 tiles
            tile = new int[30 * 32];
            // 64-byte array at the end of each nametable
            attribute = new int[64];

            name = "Nametable" + id;
            id += 1;
        }

        public int get(int addr) {
            addr &= 0x3ff;
            return addr < tile.length ? tile[addr] : attribute[addr - tile.length];
        }

        public int getAttr(int offset) {
            return get(0x3c0 | offset);
        }

        public void set(int addr, int data) {
            addr &= 0x3ff;
            if (addr < tile.length)
                tile[addr] = data;
            else
                attribute[addr - tile.length] = data;
        }
    }
}