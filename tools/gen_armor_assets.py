"""Generates placeholder textures for the omni armor (pure python PNG writer, no Pillow)."""

import os
import struct
import zlib

NS = "xiabis_modpack_toolbox"
ASSETS = os.path.join("src", "main", "resources", "assets", NS)

HEAD = [
    "................",
    "....########....",
    "..############..",
    ".##############.",
    ".####......####.",
    ".##..######..##.",
    ".##.########.##.",
    ".##.########.##.",
    ".##..#.##.#..##.",
    ".##############.",
    "..############..",
    "...##########...",
    "....DDDDDDDD....",
    "....DDDDDDDD....",
    ".....DDDDDD.....",
    "................",
]

PALETTE = {"#": (60, 24, 96, 255), "D": (34, 12, 58, 255)}


def blank(width, height):
    return [[(0, 0, 0, 0) for _ in range(width)] for _ in range(height)]


def save_png(path, width, height, pixels):
    raw = b"".join(b"\x00" + b"".join(struct.pack("BBBB", *pixels[y][x]) for x in range(width))
                   for y in range(height))
    def chunk(tag, data):
        body = tag + data
        return struct.pack(">I", len(data)) + body + struct.pack(">I", zlib.crc32(body) & 0xFFFFFFFF)
    png = b"\x89PNG\r\n\x1a\n"
    png += chunk(b"IHDR", struct.pack(">IIBBBBB", width, height, 8, 6, 0, 0, 0))
    png += chunk(b"IDAT", zlib.compress(raw, 9))
    png += chunk(b"IEND", b"")
    os.makedirs(os.path.dirname(path), exist_ok=True)
    existing = os.path.exists(path)
    if existing:
        print("keep existing", path)
        return
    with open(path, "wb") as handle:
        handle.write(png)
    print("wrote", path, len(png), "bytes")


def main():
    item = blank(16, 16)
    for y, row in enumerate(HEAD):
        for x, char in enumerate(row):
            if char in PALETTE:
                item[y][x] = PALETTE[char]
    save_png(os.path.join(ASSETS, "textures", "item", "omni_armor.png"), 16, 16, item)

    layer = blank(64, 32)
    for y in range(16):
        for x in range(32):
            shade = 70 + (x % 4) * 6 + (y % 3) * 4
            layer[y][x] = (min(255, shade + 30), 30, min(255, shade + 60), 255)
    for x in range(8, 24):
        layer[14][x] = (200, 170, 255, 255)
    save_png(os.path.join(ASSETS, "textures", "models", "armor", "omni_armor_layer_1.png"), 64, 32, layer)


if __name__ == "__main__":
    main()
