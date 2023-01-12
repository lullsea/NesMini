package net.lullsea.NesMini;

import java.io.File;
import java.nio.file.Files;
import net.lullsea.NesMini.Mapper.Mapper;

// Reads the ROM and loads it into data

/*
Header (16 bytes)
  0-3: Constant $4E $45 $53 $1A ("NES" followed by MS-DOS end-of-file)
  4: Size of PRG ROM in 16 KB units
  5: Size of CHR ROM in 8 KB units (Value 0 means the board uses CHR RAM)
  6: Flags 6 - Lower nyble of the mapper, mirroring, battery, trainer
  7: Flags 7 - Upper nyble of the Mapper, VS/Playchoice, NES 2.0
  8: Flags 8 - PRG-RAM size (rarely used extension)
  9: Flags 9 - TV system (rarely used extension)
  10: Flags 10 - TV system, PRG-RAM presence (unofficial, rarely used extension
  11-15: Unused padding (should be filled with zero, but some rippers put their name across bytes 7-15)
*/

public class RomLoader {
    public int prgCount, chrCount;
    public int[] rom, vrom;
    public boolean trainer, battery, isINES, isNES2, isNSF;
    public Mirror mirror = Mirror.UNLOADED;
    public Mapper mapper;
    private int mapperID;
    private Nes nes;

    public enum Mirror {
        UNLOADED,
        FOUR_SCREEN,
        HORIZONTAL,
        VERTICAL,
        SINGLE
    }

    RomLoader(Nes nes, File game) throws Exception {
        this.nes = nes;
        byte[] data = Files.readAllBytes(game.toPath());

        /* ----------------------- Check for valid file types ----------------------- */

        // data[0 - 3] == "NES?"
        isINES = data[0] == 0x4e && data[1] == 0x45 && data[2] == 0x53 && data[3] == 0x1a;

        isNES2 = isINES && (data[7] & 0x0c) == 0x08;

        // data[0 - 4] == "NESM?"
        isNSF = data[0] == 'N' && data[1] == 'E' && data[2] == 'S' && data[3] == 'M' && data[4] == 0x1a;

        if (isNSF)
            // Valid nsf file
            // Don't know if im going to deal with this
            System.out.println("NSF not yet implemented");

        if (!isINES)
            throw new Exception("Invalid File Format");

        /* --------------------------- Load rom to memory --------------------------- */
        // Nes 2.0 Format
        prgCount = isNES2 ? data[4] + ((data[9] & 15) << 8) : data[4];
        chrCount = isNES2 ? data[5] + ((data[9] >> 4) << 8) : data[5];
        mirror = (data[6] & 8) != 0 ? Mirror.FOUR_SCREEN : (data[6] & 1) == 0 ? Mirror.HORIZONTAL : Mirror.VERTICAL;
        trainer = (data[6] & 4) != 0;
        // Bottom 4 bits denote mapper number
        mapperID = (data[6] >> 4) + ((data[7] >> 4) << 4);
        mapper = Mapper.load(mapperID);
        mapper.nes = this.nes;
        rom = new int[(prgCount == 0) ? 0x4000 : prgCount * 0x4000]; // 1024 * 16
        vrom = new int[(chrCount == 0) ? 0x2000 : chrCount * 0x2000]; // 1024 * 8
        // Skip the header and trainer section
        int offset = 16 + (trainer ? 512 : 0);
        // populate rom
        for (int i = 0; i < rom.length; i++) {
            if (offset + i >= data.length)
                break;
            rom[i] = data[offset + i] & 0xff;
            // System.out.println(Integer.toHexString(rom[i]));
        }
        offset += 0x4000 * prgCount; // Skip rom section
        // populate vrom
        for (int i = 0; i < vrom.length; i++) {
            if (offset + i >= data.length)
                break;
            vrom[i] = data[i + offset] & 0xff;
            // System.out.println(vrom[i]);
        }
    }

    // Fetch data from CHR ROM (PPU)
    public int readVROM(int addr) {
        return vrom[addr];
    }

    // Fetch data from PRG ROM (CPU)
    public int readROM(int addr) {
        return rom[addr & ((prgCount > 1) ? 0x7fff : 0x3fff)];
    }

    @Override
    public String toString() {
        return "-ROM: " + '\n' +
                "Nes 2.0: " + isNES2 + "\n" +
                "rSize: " + prgCount + " * 16kb" + "\n" +
                "vSize: " + chrCount + " * 8kb" + "\n" +
                "Mirroring: " + mirror.toString() + "\n" +
                "battery: " + battery + "\n" +
                "trainer: " + trainer + "\n" +
                "MapperID: " + mapperID + "\n" +
                "Mapper: " + mapper.toString();
    }
}
