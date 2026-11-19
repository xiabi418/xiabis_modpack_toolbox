#!/usr/bin/env python3
"""Generates placeholder 16x16 item textures and item model JSONs for the mod.

Run from the project root:
    python3 tools/gen_assets.py
Requires Pillow.
"""
from __future__ import annotations

import json
import os
try:
    from PIL import Image, ImageDraw
except ImportError:  # pragma: no cover - textures may already exist
    Image = ImageDraw = None

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS = os.path.join(ROOT, "src", "main", "resources", "assets", "xiabis_modpack_toolbox")
TEX = os.path.join(ASSETS, "textures", "item")
MODELS = os.path.join(ASSETS, "models", "item")
NS = "xiabis_modpack_toolbox"

# (model suffix, cmd fortune, cmd silk)
TOOL_TYPES = [
    ("pickaxe", 11, 21),
    ("shovel", 12, 22),
    ("axe", 13, 23),
    ("hoe", 14, 24),
    ("shears", 15, 25),
    ("shield", 16, 26),
    ("wrench", 17, 27),
    ("staff", 18, 28),
]

HANDLE_DARK = (107, 68, 35, 255)
HANDLE_LIGHT = (139, 90, 43, 255)


def palette(silk: bool):
    if silk:
        return {
            "hi": (72, 84, 104, 255),
            "main": (166, 224, 255, 255),
            "edge": (92, 143, 214, 255),
            "dark": (52, 74, 110, 255),
            "accent": (236, 252, 255, 255),
        }
    return {
        "hi": (255, 226, 128, 255),
        "main": (255, 194, 66, 255),
        "edge": (200, 134, 13, 255),
        "dark": (122, 78, 8, 255),
        "accent": (255, 246, 214, 255),
    }


def new_img():
    return Image.new("RGBA", (16, 16), (0, 0, 0, 0))


def draw_handle(d: ImageDraw.ImageDraw):
    # Diagonal wooden handle from bottom-left to top-right.
    d.line([(3, 14), (11, 6)], fill=HANDLE_DARK, width=2)
    d.line([(4, 13), (11, 6)], fill=HANDLE_LIGHT, width=1)


def glyph(kind: str, silk: bool):
    img = new_img()
    d = ImageDraw.Draw(img)
    p = palette(silk)
    if kind == "all":
        draw_handle(d)
        d.ellipse([7, 2, 14, 9], fill=p["main"], outline=p["edge"])
        d.ellipse([9, 4, 12, 7], fill=p["accent"])
        d.polygon([(8, 0), (9, 3), (12, 3), (10, 5), (11, 8), (8, 6), (6, 3), (8, 3)],
                  fill=p["hi"])  # tiny star
        d.line([(4, 13), (4, 13)], fill=p["accent"])
        return img
    if kind == "pickaxe":
        draw_handle(d)
        d.arc([4, 0, 16, 10], 185, 355, fill=p["edge"], width=2)
        d.arc([5, 1, 15, 9], 190, 350, fill=p["main"], width=1)
    elif kind == "shovel":
        draw_handle(d)
        d.polygon([(8, 1), (14, 3), (14, 8), (11, 10), (8, 8), (8, 3)], fill=p["main"], outline=p["edge"])
        d.line([(10, 3), (12, 5)], fill=p["accent"])
    elif kind == "axe":
        draw_handle(d)
        d.polygon([(9, 1), (15, 2), (15, 8), (11, 7), (10, 4)], fill=p["main"], outline=p["edge"])
        d.line([(12, 3), (14, 4)], fill=p["accent"])
        d.line([(11, 6), (14, 7)], fill=p["dark"])
    elif kind == "hoe":
        draw_handle(d)
        d.rectangle([8, 1, 14, 3], fill=p["main"], outline=p["edge"])
        d.line([(13, 3), (13, 7)], fill=p["main"])
        d.line([(9, 2), (12, 2)], fill=p["accent"])
    elif kind == "shears":
        d.line([(2, 13), (13, 3)], fill=p["edge"], width=2)
        d.line([(2, 3), (13, 13)], fill=p["main"], width=2)
        d.ellipse([0, 12, 4, 15], outline=p["dark"], width=1)
        d.ellipse([11, 12, 15, 15], outline=p["dark"], width=1)
    elif kind == "shield":
        d.polygon([(8, 1), (14, 3), (14, 8), (8, 15), (2, 8), (2, 3)], fill=p["main"], outline=p["edge"])
        d.line([(8, 3), (8, 12)], fill=p["accent"])
        d.line([(4, 6), (12, 6)], fill=p["accent"])
    elif kind == "wrench":
        d.line([(3, 13), (10, 6)], fill=p["main"], width=3)
        d.arc([8, 0, 15, 7], 60, 300, fill=p["main"], width=2)
        d.ellipse([10, 2, 13, 5], fill=p["dark"])
        d.line([(4, 12), (8, 8)], fill=p["accent"])
    elif kind == "staff":
        d.line([(6, 15), (10, 4)], fill=HANDLE_LIGHT, width=2)
        d.line([(7, 14), (10, 5)], fill=HANDLE_DARK)
        d.ellipse([7, 0, 14, 7], fill=p["main"], outline=p["edge"])
        d.ellipse([9, 2, 12, 5], fill=p["accent"])
    elif kind == "weapon":
        # Blade + bow/crossbow hint.
        d.line([(3, 13), (12, 4)], fill=p["main"], width=2)
        d.polygon([(10, 2), (14, 2), (14, 6)], fill=p["accent"], outline=p["edge"])
        d.arc([0, 6, 7, 15], 270, 90, fill=p["edge"], width=1)
        d.line([(1, 11), (5, 9)], fill=p["dark"])
    return img


def write_models_only():
    """Writes the model JSONs when Pillow is unavailable (textures already generated)."""
    write_model("omni_tool_silk", "omni_tool_silk")
    for kind, _cmd_f, _cmd_s in TOOL_TYPES:
        write_model(f"omni_tool_fortune_{kind}", f"omni_tool_fortune_{kind}")
        write_model(f"omni_tool_silk_{kind}", f"omni_tool_silk_{kind}")
    print("generated models in", MODELS)


def save(img: Image.Image, name: str):
    os.makedirs(TEX, exist_ok=True)
    img.save(os.path.join(TEX, name + ".png"))


def write_model(name: str, texture: str, overrides=None):
    os.makedirs(MODELS, exist_ok=True)
    data = {
        "parent": "minecraft:item/handheld",
        "textures": {"layer0": f"{NS}:item/{texture}"},
    }
    if overrides:
        data["overrides"] = overrides
    with open(os.path.join(MODELS, name + ".json"), "w") as f:
        json.dump(data, f, indent=2)
        f.write("\n")


def main():
    if Image is None:
        write_models_only()
        return
    # omni tool: 9 modes x 2 enchant modes
    save(glyph("all", False), "omni_tool_fortune")
    save(glyph("all", True), "omni_tool_silk")
    overrides = [{"predicate": {"custom_model_data": 1}, "model": f"{NS}:item/omni_tool_silk"}]
    for kind, cmd_f, cmd_s in TOOL_TYPES:
        save(glyph(kind, False), f"omni_tool_fortune_{kind}")
        save(glyph(kind, True), f"omni_tool_silk_{kind}")
        overrides.append({"predicate": {"custom_model_data": cmd_f}, "model": f"{NS}:item/omni_tool_fortune_{kind}"})
        overrides.append({"predicate": {"custom_model_data": cmd_s}, "model": f"{NS}:item/omni_tool_silk_{kind}"})
    write_model("omni_tool", "omni_tool_fortune", overrides)
    write_model("omni_tool_silk", "omni_tool_silk")
    for kind, _cmd_f, _cmd_s in TOOL_TYPES:
        write_model(f"omni_tool_fortune_{kind}", f"omni_tool_fortune_{kind}")
        write_model(f"omni_tool_silk_{kind}", f"omni_tool_silk_{kind}")

    save(glyph("weapon", False), "omni_weapon")
    write_model("omni_weapon", "omni_weapon")

    print("generated textures in", TEX)
    print("generated models in", MODELS)


if __name__ == "__main__":
    main()
