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
  6: Flags 6 - Mapper, mirroring, battery, trainer
  7: Flags 7 - Mapper, VS/Playchoice, NES 2.0
  8: Flags 8 - PRG-RAM size (rarely used extension)
  9: Flags 9 - TV system (rarely used extension)
  10: Flags 10 - TV system, PRG-RAM presence (unofficial, rarely used extension
  11-15: Unused padding (should be filled with zero, but some rippers put their name across bytes 7-15)
*/

public class RomLoader {
    public int prgCount, chrCount;
    public int[] rom, vrom;
    public boolean trainer, battery, isINES, isNES2;
    public Mirror mirror = Mirror.UNLOADED;
    public Mapper mapper;
    private int mapperID;

    public enum Mirror {
        UNLOADED,
        FOUR_SCREEN,
        HORIZONTAL,
        VERTICAL,
        SINGLE
    }

    RomLoader(File game) throws Exception {
        byte[] data = Files.readAllBytes(game.toPath());
        isINES = data[0] == 'N' && data[1] == 'E' && data[2] == 'S' && data[3] == 0x1A;
        isNES2 = isINES && (data[7] & 0x0C) == 0x08;
        // Check for valid ines rom
        if (isINES) {
            // Nes 2.0 Format
            if (isNES2) {
                prgCount = data[4] + ((data[9] & 15) << 8);
                chrCount = data[5] + ((data[9] >> 4) << 8);
            } else {
                prgCount = data[4];
                chrCount = data[5];
            }
            mirror = (data[6] & 8) != 0 ? Mirror.FOUR_SCREEN : (data[6] & 1) != 0 ? Mirror.VERTICAL : Mirror.HORIZONTAL;
            trainer = (data[6] & 4) != 0;
            // Bottom 4 bits denote mapper number
            mapperID = (data[6] >> 4) + ((data[7] >> 4) << 4);
            mapper = Mapper.getMapper(mapperID);
            rom = new int[(prgCount == 0) ? 16384 : prgCount * 16834]; // 1024 * 16
            vrom = new int[(chrCount == 0) ? 8192 : chrCount * 8192]; // 1024 * 8
            // Skip the header and trainer section
            int offset = 16 + (trainer ? 512 : 0); 
            // populate rom
            for (int i = 0; i < rom.length; i++) {
                if (offset + i >= data.length)
                    break;
                rom[i] = data[offset + i] & 0xFF;
                // System.out.println(Integer.toHexString(rom[i]));
            }
            offset += 16384 * prgCount; // Skip rom section
            // populate vrom
            for (int i = 0; i < vrom.length; i++) {
                if (offset + i >= data.length)
                    break;
                vrom[i] = data[i + offset] & 0xFF;
                // System.out.println(vrom[i]);
            }
        } else if (data[0] == 'N' && data[1] == 'E' && data[2] == 'S' && data[3] == 'M' && data[4] == 0x1A) {
            // Valid nsf file
            // Don't know if im going to deal with this
            System.out.println("Not yet implemented");
        } else
            throw new Exception("Invalid file format!");
    }

    public Mirror getMirror() {
        return mirror;
    }

    public int readVROM(int addr) {
        return vrom[addr];
    }

    public void writeVROM(int addr, int data) {
        vrom[addr] = data;
    }

    public int readROM(int addr) {
        return rom[addr & ((prgCount > 1) ? 0x7FFF : 0x3FFF)];
    }

    @Override
    public String toString() {
        return "-ROM: " + '\n' +
                "Nes 2.0: " + isNES2 + "\n" +
                "rSize: " + prgCount + " * 16kb" + "\n" +
                "vSize: " + chrCount + " * 8kb" + "\n" +
                "Mirroring: " + getMirror().toString() + "\n" +
                "battery: " + battery + "\n" +
                "trainer: " + trainer + "\n" +
                "MapperID: " + mapperID + "\n" +
                "Mapper: " + mapper.toString();
    }
}
