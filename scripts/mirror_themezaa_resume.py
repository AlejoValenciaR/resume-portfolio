from __future__ import annotations

import argparse
import re
from collections import deque
from pathlib import Path
from urllib.parse import urljoin, urlparse
from urllib.request import Request, urlopen


BASE_URL = "https://themezaa.com/html/resume/default/"
GOOGLE_FONTS_URL = (
    "https://fonts.googleapis.com/css?family="
    "Roboto:400,300italic,300,100italic,100,400italic,500,500italic,700,"
    "900,900italic,700italic%7COswald:400,300,700"
)
USER_AGENT = (
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
    "AppleWebKit/537.36 (KHTML, like Gecko) "
    "Chrome/135.0.0.0 Safari/537.36"
)

THEME_PAGE_TARGETS = {
    "index.html?page=introduction": "index.html",
    "blog.html": "blog.html",
}
TEXT_EXTENSIONS = {
    ".css",
    ".html",
    ".js",
    ".svg",
    ".txt",
    ".xml",
}
LOCAL_GOOGLE_FONTS_PATH = Path("css/google-fonts.css")

ATTR_REF_PATTERN = re.compile(
    r"""(?:href|src|poster|action)\s*=\s*["']([^"']+)["']""",
    re.IGNORECASE,
)
URL_REF_PATTERN = re.compile(r"""url\(([^)]+)\)""", re.IGNORECASE)
GENERIC_REF_PATTERN = re.compile(
    r"""["']((?:\.\./|\./)?(?:css|js|images|fonts|webfonts|video)/[^"'\\<>|{} ]+\.[A-Za-z0-9]{1,8}(?:\?[^"'\\<>|{} ]+)?)["']""",
    re.IGNORECASE,
)


def fetch_bytes(url: str) -> bytes:
    request = Request(url, headers={"User-Agent": USER_AGENT})
    with urlopen(request) as response:
        return response.read()


def decode_text(content: bytes) -> str:
    for encoding in ("utf-8", "cp1252", "latin-1"):
        try:
            return content.decode(encoding)
        except UnicodeDecodeError:
            continue
    return content.decode("utf-8", errors="replace")


def is_skippable_reference(value: str) -> bool:
    candidate = value.strip().strip("'\"")
    if not candidate:
        return True
    lowered = candidate.lower()
    return (
        lowered.startswith(("#", "mailto:", "tel:", "javascript:", "data:"))
        or candidate.startswith("//")
    )


def normalize_remote_theme_url(reference: str, context_url: str) -> str | None:
    if is_skippable_reference(reference):
        return None

    cleaned_reference = reference.strip().strip("'\"")
    if (
        cleaned_reference == LOCAL_GOOGLE_FONTS_PATH.as_posix()
        or any(char in cleaned_reference for char in (" ", "<", ">", "{", "}", "|"))
        or cleaned_reference.startswith(("href=", "src="))
    ):
        return None

    absolute = urljoin(context_url, cleaned_reference)
    parsed = urlparse(absolute)
    base = urlparse(BASE_URL)
    if parsed.netloc != base.netloc:
        return None
    if not parsed.path.startswith(base.path):
        return None
    if not Path(parsed.path).suffix:
        return None
    return absolute


def extract_references(content: str) -> set[str]:
    references: set[str] = set()
    for pattern in (ATTR_REF_PATTERN, URL_REF_PATTERN, GENERIC_REF_PATTERN):
        for match in pattern.findall(content):
            references.add(match.strip().strip("'\""))
    return references


def theme_url_to_local_path(url: str) -> Path:
    parsed = urlparse(url)
    base_path = urlparse(BASE_URL).path
    relative_path = parsed.path[len(base_path):].lstrip("/")
    if not relative_path:
        relative_path = "index.html"
    if parsed.query == "page=introduction" and relative_path == "index.html":
        return Path("index.html")
    return Path(relative_path)


def write_text(path: Path, content: str) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(content, encoding="utf-8", newline="\n")


def write_bytes(path: Path, content: bytes) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_bytes(content)


def process_google_fonts(output_dir: Path) -> None:
    css_text = decode_text(fetch_bytes(GOOGLE_FONTS_URL))
    font_urls = sorted({url for url in URL_REF_PATTERN.findall(css_text)})
    for font_url in font_urls:
        absolute_url = urljoin(GOOGLE_FONTS_URL, font_url.strip().strip("'\""))
        parsed = urlparse(absolute_url)
        filename = Path(parsed.path).name
        local_font_path = Path("webfonts") / filename
        css_text = css_text.replace(font_url, f"../{local_font_path.as_posix()}")
        write_bytes(output_dir / local_font_path, fetch_bytes(absolute_url))

    write_text(output_dir / LOCAL_GOOGLE_FONTS_PATH, css_text)


def patch_html_page(content: str) -> str:
    patched = content.replace(GOOGLE_FONTS_URL, LOCAL_GOOGLE_FONTS_PATH.as_posix())
    patched = patched.replace('src="//www.youtube.com/', 'src="https://www.youtube.com/')
    return patched


def mirror(output_dir: Path) -> None:
    output_dir.mkdir(parents=True, exist_ok=True)
    process_google_fonts(output_dir)

    queue: deque[str] = deque(urljoin(BASE_URL, key) for key in THEME_PAGE_TARGETS)
    downloaded: set[str] = set()

    while queue:
        current_url = queue.popleft()
        if current_url in downloaded:
            continue

        downloaded.add(current_url)
        local_path = output_dir / theme_url_to_local_path(current_url)
        try:
            payload = fetch_bytes(current_url)
        except Exception as exc:
            print(f"Skipping {current_url}: {exc}")
            continue

        extension = local_path.suffix.lower()
        is_text_file = extension in TEXT_EXTENSIONS or not extension

        if is_text_file:
            text = decode_text(payload)
            if extension == ".html":
                text = patch_html_page(text)
            write_text(local_path, text)

            for reference in extract_references(text):
                normalized = normalize_remote_theme_url(reference, current_url)
                if normalized and normalized not in downloaded:
                    queue.append(normalized)
        else:
            write_bytes(local_path, payload)


def main() -> None:
    parser = argparse.ArgumentParser(
        description="Mirror the ThemeZaa resume theme into a local static folder."
    )
    parser.add_argument(
        "--output",
        default="src/main/resources/static/photography",
        help="Target folder for the mirrored site.",
    )
    args = parser.parse_args()

    output_dir = Path(args.output).resolve()
    mirror(output_dir)
    print(f"Mirrored ThemeZaa assets into {output_dir}")


if __name__ == "__main__":
    main()
