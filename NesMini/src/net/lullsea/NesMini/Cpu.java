package net.lullsea.NesMini;

enum StatusFlag {
    Carry(0x1),
    Zero(0x2),
    Interrupt(0x4),
    Decimal(0x8
    ),
    Break(0x10),
    Unused(0x20),
    Overflow(0x40),
    Negative(0x80);

    final int bit;

    StatusFlag(int bit) {
        this.bit = bit;
    }
}

enum AddressingMode {
    ZERO_PAGE,
    ABSOLUTE,
    IMPLIED,
    ACCUMULATOR,
    IMMEDIATE,
    RELATIVE,
    INDIRECT,
    ZERO_PAGE_X,
    ZERO_PAGE_Y,
    INDIRECT_INDEXED,
    INDEXED_INDIRECT,
    ABSOLUTE_X,
    ABSOLUTE_Y
}

public class Cpu {
    public Cpu() {
    }
}
