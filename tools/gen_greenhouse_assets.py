"""Generates the placeholder art of the omni greenhouse (16x16 block textures + 245x186 GUI).

Run from the project root: python3 tools/gen_greenhouse_assets.py
Existing PNGs are kept, so anything hand drawn in the resource pack wins.
"""
import json
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from pngtool import write_png  # noqa: E402

ROOT = os.path.abspath(os.path.join(os.path.dirname(os.path.abspath(__file__)), '..'))
ASSETS = os.path.join(ROOT, 'src/main/resources/assets/xiabis_modpack_toolbox')

CLEAR = (0, 0, 0, 0)
BLACK = (0, 0, 0, 255)
OUTLINE = (36, 36, 44, 255)
PANEL = (198, 198, 198, 255)
PANEL_DARK = (170, 170, 170, 255)
TITLE = (163, 164, 203, 255)
DARK = (53, 54, 75, 255)
SLOT = (139, 139, 139, 255)
SLOT_LIGHT = (215, 215, 215, 255)
SLOT_DARK = (90, 90, 90, 255)


def blank(width, height, color=CLEAR):
    return [[color for _ in range(width)] for _ in range(height)]


def rect(px, x0, y0, x1, y1, color):
    for y in range(max(0, y0), min(len(px), y1)):
        for x in range(max(0, x0), min(len(px[0]), x1)):
            px[y][x] = color


def frame(px, x0, y0, x1, y1, color):
    rect(px, x0, y0, x1, y0 + 1, color)
    rect(px, x0, y1 - 1, x1, y1, color)
    rect(px, x0, y0, x0 + 1, y1, color)
    rect(px, x1 - 1, y0, x1, y1, color)


def slot(px, x0, y0):
    """18x18 inventory slot with the same bevels the hand drawn sheets use."""
    rect(px, x0, y0, x0 + 18, y0 + 18, SLOT_DARK)
    rect(px, x0, y0, x0 + 17, y0 + 17, SLOT_LIGHT)
    rect(px, x0 + 1, y0 + 1, x0 + 17, y0 + 17, SLOT)


def gauge(px, x0, y0, width, height):
    rect(px, x0 - 2, y0 - 2, x0 + width + 2, y0 + height + 2, SLOT_LIGHT)
    rect(px, x0 - 1, y0 - 1, x0 + width + 1, y0 + height + 1, SLOT_DARK)
    rect(px, x0, y0, x0 + width, y0 + height, DARK)


def gui():
    width, height = 256, 256
    px = blank(width, height)

    # Panel.
    rect(px, 0, 4, 176, 186, PANEL)
    frame(px, 0, 4, 176, 186, OUTLINE)
    rect(px, 1, 5, 175, 14, TITLE)
    rect(px, 1, 14, 175, 19, PANEL_DARK)
    rect(px, 2, 19, 174, 93, DARK)
    rect(px, 0, 91, 79, 104, TITLE)
    rect(px, 0, 104, 176, 185, PANEL)
    frame(px, 0, 104, 176, 185, PANEL_DARK)
    rect(px, 1, 105, 175, 106, OUTLINE)

    # Gauges.
    gauge(px, 10, 30, 16, 50)
    gauge(px, 150, 30, 16, 50)

    # Seed slot, outputs, cards, progress.
    slot(px, 33, 30)
    for row in range(3):
        for column in range(3):
            slot(px, 55 + column * 18, 26 + row * 18)
    slot(px, 116, 52)
    slot(px, 116, 72)
    rect(px, 114, 24, 159, 31, SLOT_LIGHT)
    rect(px, 115, 25, 158, 30, SLOT_DARK)
    rect(px, 116, 26, 158, 30, DARK)

    # Player inventory and hotbar.
    for row in range(3):
        for column in range(9):
            slot(px, 8 + column * 18, 106 + row * 18)
    for column in range(9):
        slot(px, 8 + column * 18, 164)

    # Button cluster on the right (same footprint as the generator sheet).
    rect(px, 177, 20, 245, 90, PANEL)
    frame(px, 177, 20, 245, 90, OUTLINE)
    for row in range(3):
        for column in range(3):
            rect(px, 184 + column * 18, 29 + row * 18, 184 + column * 18 + 18, 29 + row * 18 + 16, SLOT_LIGHT)
            rect(px, 185 + column * 18, 30 + row * 18, 185 + column * 18 + 17, 30 + row * 18 + 15, SLOT)
            rect(px, 185 + column * 18, 30 + row * 18, 185 + column * 18 + 17, 31 + row * 18, SLOT_DARK)

    os.makedirs(os.path.join(ASSETS, 'textures/gui'), exist_ok=True)
    out = os.path.join(ASSETS, 'textures/gui/omni_greenhouse.png')
    if os.path.exists(out):
        print('keep existing', out)
    else:
        write_png(out, width, height, px)
        print('wrote', out)


def block_image(kind):
    px = blank(16, 16)
    if kind == 'top':
        rect(px, 0, 0, 16, 16, (86, 150, 96, 255))
        rect(px, 1, 1, 15, 15, (126, 196, 130, 255))
        for i in range(3, 15, 4):
            rect(px, i, 1, i + 1, 15, (86, 150, 96, 255))
            rect(px, 1, i, 15, i + 1, (86, 150, 96, 255))
        frame(px, 0, 0, 16, 16, (54, 58, 66, 255))
    elif kind == 'side':
        rect(px, 0, 0, 16, 16, (168, 172, 178, 255))
        frame(px, 0, 0, 16, 16, (96, 100, 108, 255))
        rect(px, 2, 2, 14, 14, (198, 202, 208, 255))
        rect(px, 4, 4, 12, 12, (120, 176, 124, 255))
        rect(px, 5, 5, 11, 11, (86, 150, 96, 255))
    else:
        rect(px, 0, 0, 16, 16, (168, 172, 178, 255))
        frame(px, 0, 0, 16, 16, (96, 100, 108, 255))
        rect(px, 1, 1, 15, 15, (140, 144, 152, 255))
        rect(px, 2, 3, 14, 13, (32, 44, 32, 255))
        rect(px, 3, 4, 13, 12, (72, 140, 84, 255))
        for x in range(4, 13, 2):
            rect(px, x, 6, x + 1, 11, (44, 96, 52, 255))
        rect(px, 4, 9, 12, 10, (120, 196, 132, 255))
        rect(px, 7, 2, 9, 3, (230, 216, 120, 255))
    return px


def block_textures():
    directory = os.path.join(ASSETS, 'textures/block')
    os.makedirs(directory, exist_ok=True)
    for kind in ('top', 'side', 'front'):
        out = os.path.join(directory, f'omni_greenhouse_{kind}.png')
        if os.path.exists(out):
            print('keep existing', out)
        else:
            write_png(out, 16, 16, block_image(kind))
            print('wrote', out)


def models():
    block_model = {
        'parent': 'minecraft:block/orientable',
        'textures': {
            'top': 'xiabis_modpack_toolbox:block/omni_greenhouse_top',
            'bottom': 'xiabis_modpack_toolbox:block/omni_greenhouse_top',
            'front': 'xiabis_modpack_toolbox:block/omni_greenhouse_front',
            'side': 'xiabis_modpack_toolbox:block/omni_greenhouse_side',
        },
    }
    state = {
        'variants': {
            'facing=north': {'model': 'xiabis_modpack_toolbox:block/omni_greenhouse'},
            'facing=east': {'model': 'xiabis_modpack_toolbox:block/omni_greenhouse', 'y': 90},
            'facing=south': {'model': 'xiabis_modpack_toolbox:block/omni_greenhouse', 'y': 180},
            'facing=west': {'model': 'xiabis_modpack_toolbox:block/omni_greenhouse', 'y': 270},
        }
    }
    item_model = {'parent': 'xiabis_modpack_toolbox:block/omni_greenhouse'}
    for path, data in (('models/block/omni_greenhouse.json', block_model),
                       ('blockstates/omni_greenhouse.json', state),
                       ('models/item/omni_greenhouse.json', item_model)):
        full = os.path.join(ASSETS, path)
        os.makedirs(os.path.dirname(full), exist_ok=True)
        with open(full, 'w', encoding='utf-8') as handle:
            json.dump(data, handle, indent=2)
            handle.write('\n')
        print('wrote', full)


if __name__ == '__main__':
    gui()
    block_textures()
    models()
