#!/usr/bin/env python3
"""Strip AndroidManifest deep-link hosts to only the 19 allowed sources."""
import re
from pathlib import Path

MANIFEST = Path("/home/codespace/kotatsu-ios/app/src/main/AndroidManifest.xml")

KEEP_HOSTS = {
    "mgeko.cc", "www.mgeko.cc", "www.mgeko.com",
    "omegascans.org",
    "manhwa18.cc", "manhwa18.net", "manhwa18.com",
    "toomics.com",
    "toongod.org", "www.toongod.org",
    "toonily.com", "toonily.me",
    "hotcomics.me",
    "cocomic.co",
    "kissmanga.in",
    "likemanga.ink",
    "kaliscan.io",
    "manhwaden.com", "www.manhwaden.com",
    "madaradex.org",
    "ravenscans.org", "ravenscans.com",
    "mgread.io",
    "toonhey.com", "heytoon.net",
}

text = MANIFEST.read_text(encoding="utf-8")
lines = text.splitlines(keepends=True)
out = []
removed = 0
for line in lines:
    m = re.search(r'android:host="([^"]+)"', line)
    if m and m.group(1) not in KEEP_HOSTS:
        removed += 1
        continue
    out.append(line)

MANIFEST.write_text("".join(out), encoding="utf-8")
print(f"Removed {removed} deep-link host entries")
